package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationData {
    private final String ip;
    private final String countryName;
    private final String subdivisionName;
    private final String cityName;
    private final Map<String, Object> location;
    private final Double latitude;
    private final Double longitude;
    private final String continentCode;
    private final String countryCode;
    private final String postalCode;
    private final String regionCode;
    private final String timezone;

    public LocationData(Map<Fields, Object> fields) {
        ip = (String) fields.get(Fields.IP);
        countryName = (String) fields.get(Fields.COUNTRY_NAME);
        subdivisionName = (String) fields.get(Fields.REGION_NAME);
        cityName = (String) fields.get(Fields.CITY_NAME);
        Double[] latlon = (Double[]) fields.get(Fields.LOCATION);
        continentCode = (String) fields.get(Fields.CONTINENT_CODE);
        countryCode = (String) fields.get(Fields.COUNTRY_CODE2);
        postalCode = (String) fields.get(Fields.POSTAL_CODE);
        regionCode = (String) fields.get(Fields.REGION_CODE);
        timezone = (String) fields.get(Fields.TIMEZONE);

        if (latlon != null && latlon.length == 2) {
            location = new HashMap<String, Object>() {{
                put("lat", latlon[0]);
                put("lon", latlon[1]);
            }};
        } else {
            location = null;
        }
        latitude = (Double) fields.get(Fields.LATITUDE);
        longitude = (Double) fields.get(Fields.LONGITUDE);
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> mapping = new HashMap<>();
        if (this.countryName != null)
            mapping.put(Fields.COUNTRY_NAME.fieldName(), this.countryName);
        if (this.subdivisionName != null)
            mapping.put(Fields.REGION_NAME.fieldName(), this.subdivisionName);
        if (this.cityName != null)
            mapping.put(Fields.CITY_NAME.fieldName(), this.cityName);
        if (this.location != null)
            mapping.put(Fields.LOCATION.fieldName(), this.location);
        if (this.ip != null)
            mapping.put(Fields.IP.fieldName(), this.ip);
        if (this.continentCode != null)
            mapping.put(Fields.CONTINENT_CODE.fieldName(), this.continentCode);
        if (this.countryCode != null)
            mapping.put(Fields.COUNTRY_CODE2.fieldName(), this.countryCode);
        if (this.postalCode != null)
            mapping.put(Fields.POSTAL_CODE.fieldName(), this.postalCode);
        if (this.regionCode != null)
            mapping.put(Fields.REGION_CODE.fieldName(), this.regionCode);
        if (this.timezone != null)
            mapping.put(Fields.TIMEZONE.fieldName(), this.timezone);
        return mapping;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationData that = (LocationData) o;
        return Objects.equals(ip, that.ip) && Objects.equals(countryName, that.countryName) && Objects.equals(subdivisionName, that.subdivisionName) && Objects.equals(cityName, that.cityName) && Objects.equals(location, that.location) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects.equals(continentCode, that.continentCode) && Objects.equals(countryCode, that.countryCode) && Objects.equals(postalCode, that.postalCode) && Objects.equals(regionCode, that.regionCode) && Objects.equals(timezone, that.timezone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, countryName, subdivisionName, cityName, location, latitude, longitude, continentCode, countryCode, postalCode, regionCode, timezone);
    }
}
