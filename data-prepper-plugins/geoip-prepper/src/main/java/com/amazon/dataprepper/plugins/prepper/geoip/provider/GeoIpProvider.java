package com.amazon.dataprepper.plugins.prepper.geoip.provider;

public interface GeoIpProvider {
    LocationData getDataFromIp(String IpAddress);
}
