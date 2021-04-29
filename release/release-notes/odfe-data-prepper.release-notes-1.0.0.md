# 2021-05-xx Version 1.0.0

## Highlights
* Now builds using version 1.0+ of the OpenTelemetry tracing specification
* Additional TraceGroup fields are emitted for enhanced searching and filtering with the Trace Analytics Kibana plugin
* An OpenSearch-compatible version of Data Prepper has been released under the [opensearch-project repo](https://github.com/opensearch-project/data-prepper/releases)

## Compatibility
### Issue #1
This release is compatible with unreleased features of the Trace Analytics Kibana plugin. At time of writing, these changes are currently in the main branch of the [plugin repo](https://github.com/opendistro-for-elasticsearch/trace-analytics), however they were not included in the official 1.13.2.0 ODFE bundled release. To use Data Prepper 1.0.0 at this time, please build the Trace Analytics plugin from source and install it to your ODFE cluster.

### Issue #2
This release is backwards incompatible with data emitted by alpha/beta Data Prepper versions.

If your Elasticsearch cluster has ingested data from a previous version of Data Prepper, you will need to delete the data before running this release of Data Prepper by:
1. Stopping any existing instances of Data Prepper
2. Deleting the span and service-map indices. This can be done in the Kibana UI by navigating to Dev Tools via the left sidebar, then running the following commands:
   1. DELETE /otel-v1-apm-span-*
   2. DELETE /otel-v1-apm-service-map
   3. DELETE /_template/otel-v1-apm-span-index-template
3. Starting new instances of Data Prepper 1.0.0

We know this is a poor experience and will try to avoid these kinds of breaking changes from Data Prepper 1.0.0 and Trace Analytics 1.13.2.1 onward.

