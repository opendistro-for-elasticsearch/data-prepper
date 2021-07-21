package com.amazon.dataprepper.plugins.prepper.peerforwarder.discovery;

import com.amazon.dataprepper.metrics.PluginMetrics;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.peerforwarder.PeerForwarderConfig;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.DynamicEndpointGroup;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.common.CommonPools;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesRequest;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesResponse;
import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PeerListProvider} which uses AWS CloudMap's
 * service discovery capability to discover peers.
 */
class AwsCloudMapPeerListProvider implements PeerListProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AwsCloudMapPeerListProvider.class);

    private final ServiceDiscoveryAsyncClient awsServiceDiscovery;
    private final String namespaceName;
    private final String serviceName;
    private final AwsCloudMapDynamicEndpointGroup endpointGroup;
    private final int timeToRefreshSeconds;
    private final Backoff backoff;
    private final EventLoop eventLoop;
    private final String domainName;

    AwsCloudMapPeerListProvider(
            ServiceDiscoveryAsyncClient awsServiceDiscovery,
            String namespaceName,
            String serviceName,
            int timeToRefreshSeconds,
            Backoff backoff,
            PluginMetrics pluginMetrics) {
        this.awsServiceDiscovery = Objects.requireNonNull(awsServiceDiscovery);
        this.namespaceName = Objects.requireNonNull(namespaceName);
        this.serviceName = Objects.requireNonNull(serviceName);
        this.timeToRefreshSeconds = timeToRefreshSeconds;
        this.backoff = Objects.requireNonNull(backoff);

        if(timeToRefreshSeconds < 1)
            throw new IllegalArgumentException("timeToRefreshSeconds must be positive. Actual: " + timeToRefreshSeconds);

        eventLoop = CommonPools.workerGroup().next();
        LOG.info("Using AWS CloudMap for Peer Forwarding. namespace='{}', serviceName='{}'",
                namespaceName, serviceName);

        endpointGroup = new AwsCloudMapDynamicEndpointGroup();

        domainName = serviceName + "." + namespaceName;

        pluginMetrics.gauge(PEER_ENDPOINTS, endpointGroup, group -> group.endpoints().size());
    }

    static AwsCloudMapPeerListProvider create(PluginSetting pluginSetting, PluginMetrics pluginMetrics) {

        String awsRegion = getRequiredSettingString(pluginSetting, PeerForwarderConfig.AWS_REGION);
        String namespace = getRequiredSettingString(pluginSetting, PeerForwarderConfig.AWS_CLOUD_MAP_NAMESPACE_NAME);
        String serviceName = getRequiredSettingString(pluginSetting, PeerForwarderConfig.AWS_CLOUD_MAP_SERVICE_NAME);


        Backoff standardBackoff = Backoff.exponential(1000, 32000).withJitter(0.2);
        int timeToRefreshSeconds = 20;

        ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient = ServiceDiscoveryAsyncClient
                .builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return new AwsCloudMapPeerListProvider(
                serviceDiscoveryAsyncClient,
                namespace,
                serviceName,
                timeToRefreshSeconds,
                standardBackoff,
                pluginMetrics);
    }

    private static String getRequiredSettingString(PluginSetting pluginSetting, String propertyName) {
        String domainName = pluginSetting.getStringOrDefault(propertyName, null);
        return Objects.requireNonNull(domainName, String.format("Missing '%s' configuration value", propertyName));
    }

    @Override
    public List<String> getPeerList() {

        return endpointGroup.endpoints()
                .stream()
                .map(Endpoint::ipAddr)
                .collect(Collectors.toList());
    }

    @Override
    public void addListener(Consumer<? super List<Endpoint>> listener) {

        endpointGroup.addListener(listener);

    }

    @Override
    public void removeListener(Consumer<?> listener) {

        endpointGroup.removeListener(listener);

    }

    /**
     * The {@link DynamicEndpointGroup} class serves as a useful base class for updating
     * endpoints by supporting the endpoint observer pattern for us. We just need to
     * periodically check for updates and call {@link DynamicEndpointGroup#setEndpoints(Iterable)}.
     */
    private class AwsCloudMapDynamicEndpointGroup extends DynamicEndpointGroup {

        private int failedAttemptCount = 0;

        private AwsCloudMapDynamicEndpointGroup() {
            eventLoop.execute(this::discoverInstances);
        }

        private void discoverInstances() {
            if (isClosing()) {
                return;
            }

            DiscoverInstancesRequest discoverInstancesRequest = DiscoverInstancesRequest
                    .builder()
                    .namespaceName(namespaceName)
                    .serviceName(serviceName)
                    .build();

            LOG.info("Discovering instances.");

            awsServiceDiscovery.discoverInstances(discoverInstancesRequest).whenComplete(
                    (discoverInstancesResponse, throwable) -> {
                        if(discoverInstancesResponse != null) {
                            try {
                                failedAttemptCount = 0;
                                updateEndpointsWithDiscoveredInstances(discoverInstancesResponse);
                            } catch (Throwable ex) {
                                LOG.warn("Failed to update endpoints.", ex);
                            }
                            finally {
                                eventLoop.schedule(this::discoverInstances,
                                        timeToRefreshSeconds, TimeUnit.SECONDS);
                            }
                        }

                        if(throwable != null) {
                            failedAttemptCount++;
                            final long delayMillis = backoff.nextDelayMillis(failedAttemptCount);
                            LOG.error("Failed to discover instances for: namespace='{}', serviceName='{}'. Will retry in {} ms.",
                                    namespaceName, serviceName, delayMillis, throwable);

                            eventLoop.schedule(this::discoverInstances,
                                    delayMillis, TimeUnit.MILLISECONDS);
                        }

                    });
        }

        private void updateEndpointsWithDiscoveredInstances(DiscoverInstancesResponse discoverInstancesResponse) {
            List<HttpInstanceSummary> instances = discoverInstancesResponse.instances();

            LOG.info("Discovered {} instances.", instances.size());

            List<Endpoint> endpoints = instances
                    .stream()
                    .map(HttpInstanceSummary::attributes)
                    .map(attributes -> attributes.get("AWS_INSTANCE_IPV4"))
                    .map(ip -> Endpoint.of(domainName).withIpAddr(ip))
                    .collect(Collectors.toList());

            setEndpoints(endpoints);
        }

    }
}
