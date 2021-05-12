package com.amazon.dataprepper.parser.model;

import com.amazon.dataprepper.pipeline.server.CloudWatchMeterRegistryProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public enum MeterRegistryType {
    Prometheus,
    CloudWatch;

    public static MeterRegistry getDefaultRegistryForType(final MeterRegistryType meterRegistryType) {
        MeterRegistry meterRegistry = null;
        switch (meterRegistryType) {
            case Prometheus:
                meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                break;
            case CloudWatch:
                meterRegistry = new CloudWatchMeterRegistryProvider().getCloudWatchMeterRegistry();
                break;
        }
        return meterRegistry;
    }
}