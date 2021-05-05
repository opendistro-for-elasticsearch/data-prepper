package com.amazon.dataprepper.pipeline.server;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Provides {@link CloudWatchMeterRegistry} that enables publishing metrics to AWS Cloudwatch. Registry
 * uses the default aws credentials (i.e. credentials from .aws directory;
 * refer https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html#credentials-file-format).
 * {@link CloudWatchMeterRegistryProvider} also has a constructor with {@link CloudWatchAsyncClient} that will be used
 * for communication with Cloudwatch.
 */
public class CloudWatchMeterRegistryProvider {
    private static final String CLOUDWATCH_PROPERTIES = "cloudwatch.properties";
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusMetricsHandler.class);

    private final CloudWatchMeterRegistry cloudWatchMeterRegistry;

    public CloudWatchMeterRegistryProvider() {
        this(CLOUDWATCH_PROPERTIES, CloudWatchAsyncClient.create());
    }

    public CloudWatchMeterRegistryProvider(
            final String cwPropertiesFile,
            final CloudWatchAsyncClient cloudWatchClient) {
        final CloudWatchConfig cloudWatchConfig = createCloudWatchConfig(cwPropertiesFile);
        this.cloudWatchMeterRegistry = new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchClient);
    }

    /**
     * Returns the CloudWatchMeterRegistry created using the default aws credentials
     */
    public CloudWatchMeterRegistry getCloudWatchMeterRegistry() {
        return this.cloudWatchMeterRegistry;
    }

    /**
     * Returns CloudWatchConfig using the properties from {@link #CLOUDWATCH_PROPERTIES}
     */
    private CloudWatchConfig createCloudWatchConfig(final String cwPropertiesFile) {
        CloudWatchConfig cloudWatchConfig = null;
        try (final InputStream inputStream = Objects.requireNonNull(getClass()
                .getClassLoader().getResourceAsStream(cwPropertiesFile))) {
            final Properties cloudwatchProperties = new Properties();
            cloudwatchProperties.load(inputStream);
            cloudWatchConfig = cloudwatchProperties::getProperty; //overriding getKey() from CloudWatchConfig
        } catch (IOException ex) {
            LOG.error("Encountered exception in creating CloudWatchConfig for CloudWatchMeterRegistry, " +
                    "Proceeding without metrics", ex);
            //If there is no registry attached, micrometer will make NoopMeters which are discarded.
        }
        return cloudWatchConfig;
    }
}
