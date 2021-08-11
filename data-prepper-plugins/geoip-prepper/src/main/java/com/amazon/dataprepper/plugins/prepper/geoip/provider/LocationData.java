package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationData {

    private final String CITY_FIELD = "geo.city_name";
    private final String COUNTRY_FIELD = "geo.country_name";
    private final String CONTINENT_FIELD = "geo.continent_code";
    private final String COUNTRY_ISO_FIELD = "geo.country_iso_code";
    private final String POSTAL_FIELD = "geo.postal_code";
    private final String REGION_FIELD = "geo.region_name";
    private final String REGION_CODE_FIELD = "geo.region_code";
    private final String TIMEZONE_FIELD = "geo.timezone";
    private final String LOCATION_FIELD = "geo.location";
    private final String LATITUDE_FIELD = "geo.location.lat";
    private final String LONGITUDE_FIELD = "geo.location.lon";
    private final String ip;

    @JsonProperty(COUNTRY_FIELD)
    private final String countryName;
    @JsonProperty(REGION_FIELD)
    private final String regionName;
    @JsonProperty(CITY_FIELD)
    private final String cityName;
    @JsonProperty(LOCATION_FIELD)
    private final Map<String, Double> location;
    @JsonProperty(LATITUDE_FIELD)
    private final Double latitude;
    @JsonProperty(LONGITUDE_FIELD)
    private final Double longitude;
    @JsonProperty(CONTINENT_FIELD)
    private final String continentCode;
    @JsonProperty(COUNTRY_ISO_FIELD)
    private final String countryCode;
    @JsonProperty(POSTAL_FIELD)
    private final String postalCode;
    @JsonProperty(REGION_CODE_FIELD)
    private final String regionCode;
    @JsonProperty(TIMEZONE_FIELD)
    private final String timeZone;

    public LocationData(Map<GeoDataField, Object> fields) {
        ip = (String) fields.get(GeoDataField.IP);
        countryName = (String) fields.get(GeoDataField.COUNTRY_NAME);
        regionName = (String) fields.get(GeoDataField.REGION_NAME);
        cityName = (String) fields.get(GeoDataField.CITY_NAME);
        Double[] latAndLong = (Double[]) fields.get(GeoDataField.LOCATION);
        continentCode = (String) fields.get(GeoDataField.CONTINENT_CODE);
        countryCode = (String) fields.get(GeoDataField.COUNTRY_ISO_CODE);
        postalCode = (String) fields.get(GeoDataField.POSTAL_CODE);
        regionCode = (String) fields.get(GeoDataField.REGION_CODE);
        timeZone = (String) fields.get(GeoDataField.TIMEZONE);

        if (latAndLong != null && latAndLong.length == 2) {
            location = new HashMap<String, Double>() {{
                put("lat", latAndLong[0]);
                put("lon", latAndLong[1]);
            }};
        } else {
            location = null;
        }
        latitude = (Double) fields.get(GeoDataField.LATITUDE);
        longitude = (Double) fields.get(GeoDataField.LONGITUDE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationData that = (LocationData) o;
        return Objects.equals(ip, that.ip) && Objects.equals(countryName, that.countryName) && Objects.equals(regionName, that.regionName) && Objects.equals(cityName, that.cityName) && Objects.equals(location, that.location) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects.equals(continentCode, that.continentCode) && Objects.equals(countryCode, that.countryCode) && Objects.equals(postalCode, that.postalCode) && Objects.equals(regionCode, that.regionCode) && Objects.equals(timeZone, that.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, countryName, regionName, cityName, location, latitude, longitude, continentCode, countryCode, postalCode, regionCode, timeZone);
    }
}
