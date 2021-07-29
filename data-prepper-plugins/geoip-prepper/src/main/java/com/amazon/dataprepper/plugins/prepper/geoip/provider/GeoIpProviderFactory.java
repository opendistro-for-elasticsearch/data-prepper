package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIPPrepperConfig;

import java.util.Objects;

public class GeoIpProviderFactory {
    public GeoIpProvider createGeoIpProvider(final PluginSetting pluginSetting) {
        Objects.requireNonNull(pluginSetting);
        final String providerString = pluginSetting.getStringOrDefault(GeoIPPrepperConfig.DATA_SOURCE, null);
        Objects.requireNonNull(providerString, String.format("Missing '%s' configuration value", GeoIPPrepperConfig.DATA_SOURCE));

        final DataSource provider = DataSource.valueOf(providerString);
        switch (provider) {
            case MaxMindGeolite2CityDatabase:
                return new MaxMindGeoIpProvider(pluginSetting);
            default:
                throw new UnsupportedOperationException(String.format("Unsupported data source: %s", provider));

        }
    }
}
