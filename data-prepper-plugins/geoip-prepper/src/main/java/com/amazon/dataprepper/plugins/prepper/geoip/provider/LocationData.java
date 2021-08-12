package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationData {
    private final String CITY_FIELD = "geo.city_name";
    private final String COUNTRY_NAME_FIELD = "geo.country_name";
    private final String CONTINENT_FIELD = "geo.continent_code";
    private final String COUNTRY_ISO_FIELD = "geo.country_iso_code";
    private final String POSTAL_CODE_FIELD = "geo.postal_code";
    private final String REGION_NAME_FIELD = "geo.region_name";
    private final String REGION_CODE_FIELD = "geo.region_code";
    private final String TIMEZONE_FIELD = "geo.timezone";
    private final String LOCATION_FIELD = "geo.location";
    private final String LATITUDE_FIELD = "geo.location.lat";
    private final String LONGITUDE_FIELD = "geo.location.lon";
    private final String ip;

    @JsonProperty(COUNTRY_NAME_FIELD)
    private final String countryName;
    @JsonProperty(REGION_NAME_FIELD)
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
    @JsonProperty(POSTAL_CODE_FIELD)
    private final String postalCode;
    @JsonProperty(REGION_CODE_FIELD)
    private final String regionCode;
    @JsonProperty(TIMEZONE_FIELD)
    private final String timeZone;

    private LocationData(Builder builder) {
        ip = builder.ip;
        countryName = builder.countryName;
        regionName = builder.regionName;
        cityName = builder.cityName;
        Double[] latAndLong = builder.location;
        continentCode = builder.continentCode;
        countryCode = builder.countryCode;
        postalCode = builder.postalCode;
        regionCode = builder.regionCode;
        timeZone = builder.timeZone;
        //TODO Investigate how location is seen in ElasticSearch and determine the best way to store the Lat/Lon point
        if (latAndLong != null && latAndLong.length == 2) {
            location = new HashMap<String, Double>() {{
                put("lat", latAndLong[0]);
                put("lon", latAndLong[1]);
            }};
        } else if ((builder.latitude != null) && (builder.longitude != null)){
            location = new HashMap<String, Double>() {{
                put("lat", builder.latitude);
                put("lon", builder.longitude);
            }};
        } else {
            location = null;
        }
        latitude = builder.latitude;
        longitude = builder.longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationData that = (LocationData) o;
        return Objects.equals(ip, that.ip)
                && Objects.equals(countryName, that.countryName)
                && Objects.equals(regionName, that.regionName)
                && Objects.equals(cityName, that.cityName)
                && Objects.equals(location, that.location)
                && Objects.equals(latitude, that.latitude)
                && Objects.equals(longitude, that.longitude)
                && Objects.equals(continentCode, that.continentCode)
                && Objects.equals(countryCode, that.countryCode)
                && Objects.equals(postalCode, that.postalCode)
                && Objects.equals(regionCode, that.regionCode)
                && Objects.equals(timeZone, that.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip,
                countryName,
                regionName,
                cityName,
                location,
                latitude,
                longitude,
                continentCode,
                countryCode,
                postalCode,
                regionCode,
                timeZone);
    }

    public static final class Builder {
        private String ip;
        private String countryName;
        private String regionName;
        private String cityName;
        private Double[] location;
        private Double latitude;
        private Double longitude;
        private String continentCode;
        private String countryCode;
        private String postalCode;
        private String regionCode;
        private String timeZone;

        public Builder withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder withCityName(String city) {
            this.cityName = city;
            return this;
        }

        public Builder withCountry(String countryName) {
            this.countryName = countryName;
            return this;
        }

        public Builder withContinent(String continent) {
            this.continentCode = continent;
            return this;
        }

        public Builder withCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder withRegionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public Builder withRegionCode(String regionCode) {
            this.regionCode = regionCode;
            return this;
        }

        public Builder withTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder withLocation(Double[] location) {
            if (location.length != 2) {
                throw new IllegalArgumentException("location must be an array of length 2 in form [lat, long]");
            }
            this.location = location;
            return this;
        }

        public Builder withLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public LocationData build() {
            return new LocationData(this);
        }
    }
}

