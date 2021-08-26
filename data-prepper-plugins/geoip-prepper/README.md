# GeoIP Prepper
The GeoIP prepper is a prepper plugin to add geolocation data to documents based off an IP address. 

## Supported Databases
Currently, the prepper only supports MaxMind City databases. To reduce the size of the project, users should supply their own database file. A free version of the database can be found [here.](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)

## Usages
Example `.yaml` configuration
```
prepper:
    - geoip_prepper:
        target_field: "span.attributes.net@peer@ip"
        database_path: "path/to/database.mmdb
        desired_fields: ["location", "city_name", "country_name"]
```

## Configuration
- target_field(required): A string representing the field on the documents in which IP should be read from.
- database_path (required): A string representing the path to the database file. Currently, .mmdb files are supported. The Geolite2 City Database can be downloaded via [the MaxMind website](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data).
- desired_fields (optional): An array of strings representing the fields to be appended to the documents. Available fields are:
  - ip, city_name, country_name, continent_code, country_iso_code, postal_code, region_name, region_code, timezone, location, latitude, longitude
    
  This value defaults to include all available fields.

<!TODO: Update when new data_sources are added>

## Location Details
The "location" field is an object containing a latitude and longitude. The default index template provided with Data Prepper maps this field to a [geo_point type](https://www.elastic.co/guide/en/elasticsearch/reference/7.14/geo-point.html), which enables geospatial queries in Elasticsearch as well as fancy map visualizations in Kibana. 
## Developer Guide
This plugin is compatible with Java 8. See
- [CONTRIBUTING](https://github.com/opendistro-for-elasticsearch/data-prepper/blob/main/CONTRIBUTING.md)
- [monitoring](https://github.com/opendistro-for-elasticsearch/data-prepper/blob/main/docs/readme/monitoring.md)
