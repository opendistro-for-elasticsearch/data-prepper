package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIpPrepperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoIpProviderFactoryTest {

    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";
    private static final String[] TEST_DESIRED_FIELDS = {"city_name", "country_name", "location"};

    private PluginSetting pluginSetting;

    private GeoIpProviderFactory factory;

    @BeforeEach
    void setup() {
        factory = new GeoIpProviderFactory();
        pluginSetting = new PluginSetting(
                "geoip",
                new HashMap<String, Object>() {{
                    put(GeoIpPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                    put(GeoIpPrepperConfig.DESIRED_FIELDS, TEST_DESIRED_FIELDS);
                }}
        );
    }


    @Test
    void testCreateGeoIpProviderThrowsWhenNoProviderIsProvided() {
        assertThrows(NullPointerException.class,
                () -> factory.createGeoIpProvider(pluginSetting));
    }

    @Test
    void testCreateGeoIpProviderThrowsForUndefinedProvider() {
        // Api lookup is currently unsupported.
        pluginSetting.getSettings().put(GeoIpPrepperConfig.DATA_SOURCE, DataSource.MaxMindApiCityDatabase.toString());

        assertThrows(UnsupportedOperationException.class,
                () -> factory.createGeoIpProvider(pluginSetting));
    }

    @Test
    void testCreateGeoIpProviderCreatesMaxMindProvider() {
        pluginSetting.getSettings().put(GeoIpPrepperConfig.DATA_SOURCE, DataSource.MaxMindGeolite2CityDatabase.toString());
        GeoIpProvider actual = factory.createGeoIpProvider(pluginSetting);

        assertTrue(actual instanceof MaxMindGeoIpProvider);
    }
}
