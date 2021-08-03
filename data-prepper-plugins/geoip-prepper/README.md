# GeoIP Prepper


## Usages
Example `.yaml` configuration
```
prepper:
    - geoip_prepper:
        target_field: "span.attributes.net@peer@ip"
        database_path: "path/to/database.mmdb
```

## Configuration
- target_field(required): A string representing the field on the documents in which IP should be read from.
- database_path (required): A string representing the path to the database file. Currently, .mmdb files are supported. The Geolite2 City Database can be downloaded via [the MaxMind website](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data).
- data_source (optional): A string representing the type of lookup to be performed. Currently, only [MaxMind City Databases](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data) are supported.
## Metrics
Apart from common metrics in [AbstractPrepper](https://github.com/opendistro-for-elasticsearch/data-prepper/blob/main/data-prepper-api/src/main/java/com/amazon/dataprepper/model/prepper/AbstractPrepper.java), geoip-prepper introduces the following custom metrics.
- Currently, the prepper tracks no additional metrics.

<!-- TODO add metrics -->

### Counter


## Developer Guide
This plugin is compatible with Java 8. See
- [CONTRIBUTING](https://github.com/opendistro-for-elasticsearch/data-prepper/blob/main/CONTRIBUTING.md)
- [monitoring](https://github.com/opendistro-for-elasticsearch/data-prepper/blob/main/docs/readme/monitoring.md)
