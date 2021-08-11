package com.amazon.dataprepper.plugins.prepper.geoip.provider;

public enum GeoDataField {
    IP("ip"),
    CITY_NAME("geo.city_name"),
    COUNTRY_NAME("geo.country_name"),
    CONTINENT_CODE("geo.continent_code"),
    COUNTRY_ISO_CODE("geo.country_iso_code"),
    POSTAL_CODE("geo.postal_code"),
    REGION_NAME("geo.region_name"),
    REGION_CODE("geo.region_code"),
    TIMEZONE("geo.timezone"),
    LOCATION("geo.location"),
    LATITUDE("geo.location.lat"),
    LONGITUDE("geo.location.lon");

    private final String fieldName;

    GeoDataField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
