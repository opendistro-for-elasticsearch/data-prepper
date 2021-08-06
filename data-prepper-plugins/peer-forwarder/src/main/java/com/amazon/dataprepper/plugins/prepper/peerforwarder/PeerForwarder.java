package com.amazon.dataprepper.plugins.prepper.peerforwarder;

import com.amazon.dataprepper.model.PluginType;
import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.prepper.AbstractPrepper;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.plugins.prepper.peerforwarder.discovery.StaticPeerListProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@DataPrepperPlugin(name = "peer_forwarder", type = PluginType.PREPPER)
public class PeerForwarder extends AbstractPrepper<Record<ExportTraceServiceRequest>, Record<ExportTraceServiceRequest>> {
    public static final String REQUESTS = "requests";
    public static final String LATENCY = "latency";
    public static final String ERRORS = "errors";
    public static final String DESTINATION = "destination";

    public static final int ASYNC_REQUEST_THREAD_COUNT = 200;

    private static final Logger LOG = LoggerFactory.getLogger(PeerForwarder.class);

    private final HashRing hashRing;
    private final PeerClientPool peerClientPool;
    private final int maxNumSpansPerRequest;

    private final Map<String, Timer> forwardRequestTimers;
    private final Map<String, Counter> forwardedRequestCounters;
    private final Map<String, Counter> forwardRequestErrorCounters;

    private final ExecutorService executorService;

    public PeerForwarder(final PluginSetting pluginSetting,
                         final PeerClientPool peerClientPool,
                         final HashRing hashRing,
                         final int maxNumSpansPerRequest) {
        super(pluginSetting);
        this.peerClientPool = peerClientPool;
        this.hashRing = hashRing;
        this.maxNumSpansPerRequest = maxNumSpansPerRequest;
        forwardedRequestCounters = new ConcurrentHashMap<>();
        forwardRequestErrorCounters = new ConcurrentHashMap<>();
        forwardRequestTimers = new ConcurrentHashMap<>();

        executorService = Executors.newFixedThreadPool(ASYNC_REQUEST_THREAD_COUNT);
    }

    public PeerForwarder(final PluginSetting pluginSetting) {
        this(pluginSetting, PeerForwarderConfig.buildConfig(pluginSetting));
    }

    public PeerForwarder(final PluginSetting pluginSetting, final PeerForwarderConfig peerForwarderConfig) {
        this(
                pluginSetting,
                peerForwarderConfig.getPeerClientPool(),
                peerForwarderConfig.getHashRing(),
                peerForwarderConfig.getMaxNumSpansPerRequest()
        );
    }

    @Override
    public List<Record<ExportTraceServiceRequest>> doExecute(final Collection<Record<ExportTraceServiceRequest>> records) {
        final Map<String, List<ResourceSpans>> groupedRS = new HashMap<>();

        // Group ResourceSpans by consistent hashing of traceId
        for (final Record<ExportTraceServiceRequest> record : records) {
            for (final ResourceSpans rs : record.getData().getResourceSpansList()) {
                final List<Map.Entry<String, ResourceSpans>> rsBatch = PeerForwarderUtils.splitByTrace(rs);
                for (final Map.Entry<String, ResourceSpans> entry : rsBatch) {
                    final String traceId = entry.getKey();
                    final ResourceSpans newRS = entry.getValue();
                    final String dataPrepperIp = hashRing.getServerIp(traceId).orElse(StaticPeerListProvider.LOCAL_ENDPOINT);
                    groupedRS.computeIfAbsent(dataPrepperIp, x -> new ArrayList<>()).add(newRS);
                }
            }
        }

        // Buffer of requests to be exported to the downstream of the local data-prepper
        final List<Record<ExportTraceServiceRequest>> results = new LinkedList<>();

        final List<CompletableFuture<Record>> futures = new LinkedList<>();

        for (final Map.Entry<String, List<ResourceSpans>> entry : groupedRS.entrySet()) {
            final TraceServiceGrpc.TraceServiceBlockingStub client;
            if (isAddressDefinedLocally(entry.getKey())) {
                client = null;
            } else {
                client = peerClientPool.getClient(entry.getKey());
            }

            // Create ExportTraceRequest for storing single batch of spans
            ExportTraceServiceRequest.Builder currRequestBuilder = ExportTraceServiceRequest.newBuilder();
            int currSpansCount = 0;
            for (final ResourceSpans rs : entry.getValue()) {
                final int rsSize = PeerForwarderUtils.getResourceSpansSize(rs);
                if (currSpansCount >= maxNumSpansPerRequest) {
                    final ExportTraceServiceRequest currRequest = currRequestBuilder.build();
                    if (client == null) {
                        results.add(new Record<>(currRequest));
                    } else {
                        futures.add(processRequest(client, currRequest));
                    }
                    currRequestBuilder = ExportTraceServiceRequest.newBuilder();
                    currSpansCount = 0;
                }
                currRequestBuilder.addResourceSpans(rs);
                currSpansCount += rsSize;
            }
            // Dealing with the last batch request
            if (currSpansCount > 0) {
                final ExportTraceServiceRequest currRequest = currRequestBuilder.build();
                if (client == null) {
                    results.add(new Record<>(currRequest));
                } else {
                    futures.add(processRequest(client, currRequest));
                }
            }
        }

        for (final CompletableFuture<Record> future : futures) {
            try {
                final Record record = future.get();
                if (record != null) {
                    results.add(record);
                }
            } catch (Exception e) {
                LOG.error("Problem with asynchronous peer forwarding", e);
            }
        }

        return results;
    }

    /**
     * Asynchronously forwards a request to the peer address. Returns a record with an empty payload if
     * the request succeeds, otherwise the payload will contain the failed ExportTraceServiceRequest to
     * be processed locally.
     */
    private CompletableFuture<Record> processRequest(final TraceServiceGrpc.TraceServiceBlockingStub client,
                                                     final ExportTraceServiceRequest request) {
        final String peerIp = client.getChannel().authority();
        final Timer forwardRequestTimer = forwardRequestTimers.computeIfAbsent(
                peerIp, ip -> pluginMetrics.timerWithTags(LATENCY, DESTINATION, ip));
        final Counter forwardedRequestCounter = forwardedRequestCounters.computeIfAbsent(
                peerIp, ip -> pluginMetrics.counterWithTags(REQUESTS, DESTINATION, ip));
        final Counter forwardRequestErrorCounter = forwardRequestErrorCounters.computeIfAbsent(
                peerIp, ip -> pluginMetrics.counterWithTags(ERRORS, DESTINATION, ip));

        final CompletableFuture<Record> callFuture = CompletableFuture.supplyAsync(() ->
        {
            forwardedRequestCounter.increment();
            try {
                forwardRequestTimer.record(() -> client.export(request));
                return null;
            } catch (Exception e) {
                LOG.error("Failed to forward request to address: {}", peerIp, e);
                forwardRequestErrorCounter.increment();
                return new Record<>(request);
            }
        }, executorService);

        return callFuture;
    }

    private boolean isAddressDefinedLocally(final String address) {
        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            return false;
        }
        if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
            return true;
        } else {
            try {
                return NetworkInterface.getByInetAddress(inetAddress) != null;
            } catch (SocketException e) {
                return false;
            }
        }
    }


    @Override
    public void prepareForShutdown() {

    }

    @Override
    public boolean isReadyForShutdown() {
        return true;
    }

    @Override
    public void shutdown() {
        //TODO: cleanup resources
    }
}
