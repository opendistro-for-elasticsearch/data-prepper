package com.amazon.dataprepper.benchmarks.prepper;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.plugins.prepper.oteltrace.OTelTraceRawPrepper;
import com.google.protobuf.ByteString;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class OtelTraceRawPrepperBenchmarks {

    @State(Scope.Benchmark)
    public static class OtelTraceRawPrepperState {
        private OTelTraceRawPrepper otelTraceRawPrepper;

        @Param(value = "16")
        private int concurrencyScale;

        @Setup(Level.Trial)
        public void setupOtelTraceRawPrepper() {
            PluginSetting pluginSetting = new PluginSetting("otel_trace_raw_prepper", new HashMap<>()) {{
                setPipelineName("test-pipeline");
                setProcessWorkers(concurrencyScale);
            }};
            otelTraceRawPrepper = new OTelTraceRawPrepper(pluginSetting);
        }

        @TearDown(Level.Trial)
        public void shutdownOtelTraceRawPrepper() {
            otelTraceRawPrepper.shutdown();
        }
    }

    @State(Scope.Thread)
    public static class TestDataState {
        private List<byte[]> spanIds;
        private List<byte[]> traceIds;
        private static final Random RANDOM = new Random();
        private static final List<String> serviceNames = Arrays.asList("FRONTEND", "BACKEND", "PAYMENT", "CHECKOUT", "DATABASE");
        private static final List<String> traceGroups = Arrays.asList("tg1", "tg2", "tg3", "tg4", "tg5", "tg6", "tg7", "tg8", "tg9");
        private List<Record<ExportTraceServiceRequest>> batch;

        @Param(value = "100")
        private int batchSize;

        /**
         * Gets a new trace id. 10% of the time it will generate a new id, and otherwise will pick a random
         * trace id that has already been generated
         */
        private byte[] getTraceId() {
            if(RANDOM.nextInt(100) < 10 || traceIds.isEmpty()) {
                final byte[] traceId = getRandomBytes(16);
                traceIds.add(traceId);
                return traceId;
            } else {
                return traceIds.get(RANDOM.nextInt(traceIds.size()));
            }
        }

        /**
         * Gets a span id and adds to the list of existing span ids
         */
        private byte[] getSpanId() {
            final byte[] spanId = getRandomBytes(8);
            spanIds.add(spanId);
            return spanId;
        }

        /**
         * Gets a parent id. 5% of the time will return null, indicating a root span. Otherwise picks a random
         * spanid that is already existing
         */
        private byte[] getParentId() {
            if(RANDOM.nextInt(20) == 0 || spanIds.isEmpty()) {
                return null;
            } else {
                return spanIds.get(RANDOM.nextInt(spanIds.size()));
            }
        }

        @Setup(Level.Trial)
        public void resetTraceSpanIdCaches() throws UnsupportedEncodingException {
            spanIds = new ArrayList<>();
            traceIds = new ArrayList<>();
        }

        @Setup(Level.Invocation)
        public void generateBatch() throws UnsupportedEncodingException {
            batch = new ArrayList<>();
            for(int j=0; j<batchSize; j++) {
                batch.add(new Record<>(getExportTraceServiceRequest(
                        getResourceSpans(
                                serviceNames.get(RANDOM.nextInt(serviceNames.size())),
                                traceGroups.get(RANDOM.nextInt(traceGroups.size())),
                                getSpanId(),
                                getParentId(),
                                getTraceId(),
                                Span.SpanKind.SPAN_KIND_CLIENT
                        ))));
            }
        }

        private static byte[] getRandomBytes(int len) {
            byte[] bytes = new byte[len];
            RANDOM.nextBytes(bytes);
            return bytes;
        }

        public static ResourceSpans getResourceSpans(final String serviceName, final String spanName, final byte[]
                spanId, final byte[] parentId, final byte[] traceId, final Span.SpanKind spanKind) throws UnsupportedEncodingException {
            final ByteString parentSpanId = parentId != null ? ByteString.copyFrom(parentId) : ByteString.EMPTY;
            return ResourceSpans.newBuilder()
                    .setResource(
                            Resource.newBuilder()
                                    .addAttributes(KeyValue.newBuilder()
                                            .setKey("service.name")
                                            .setValue(AnyValue.newBuilder().setStringValue(serviceName).build()).build())
                                    .build()
                    )
                    .addInstrumentationLibrarySpans(
                            0,
                            InstrumentationLibrarySpans.newBuilder()
                                    .addSpans(
                                            Span.newBuilder()
                                                    .setName(spanName)
                                                    .setKind(spanKind)
                                                    .setSpanId(ByteString.copyFrom(spanId))
                                                    .setParentSpanId(parentSpanId)
                                                    .setTraceId(ByteString.copyFrom(traceId))
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
        }

        public static ExportTraceServiceRequest getExportTraceServiceRequest(ResourceSpans...spans){
            return ExportTraceServiceRequest.newBuilder()
                    .addAllResourceSpans(Arrays.asList(spans))
                    .build();
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    public void benchmarkExecute(OtelTraceRawPrepperState otelTraceRawPrepperState, TestDataState testDataState) {
        otelTraceRawPrepperState.otelTraceRawPrepper.execute(testDataState.batch);
    }
}
