package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import java.util.Optional;

public interface GeoIpProvider {
    Optional<LocationData> getDataFromIp(final String IpAddress);
}
