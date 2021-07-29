package com.amazon.dataprepper.plugins.prepper.geoip.provider;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIPPrepper;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIPPrepperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class GeoIpProviderFactoryTest {

    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";

    private PluginSetting pluginSetting;

    private GeoIpProviderFactory factory;

    @BeforeEach
    void setup() {
        factory = new GeoIpProviderFactory();
        pluginSetting = new PluginSetting(
                "geoip",
                new HashMap<String, Object>() {{
                    put(GeoIPPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                }}
        );
    }


    @Test
    void createGeoIpProvider_throws_when_no_provider_is_provided() {
        assertThrows(NullPointerException.class,
                () -> factory.createGeoIpProvider(pluginSetting));
    }

    @Test
    void createGeoIpProvider_throws_for_undefined_provider() {
        // Api lookup is currently unsupported.
        pluginSetting.getSettings().put(GeoIPPrepperConfig.DATA_SOURCE, DataSource.MaxMindApiCityDatabase.toString());

        assertThrows(UnsupportedOperationException.class,
                () -> factory.createGeoIpProvider(pluginSetting));
    }

    @Test
    void createGeoIpProvider_creates_MaxMind_provider() {
        pluginSetting.getSettings().put(GeoIPPrepperConfig.DATA_SOURCE, DataSource.MaxMindGeolite2CityDatabase.toString());
        GeoIpProvider expected = new MaxMindGeoIpProvider(pluginSetting);
        GeoIpProvider actual = factory.createGeoIpProvider(pluginSetting);

        assertTrue(new ReflectionEquals(actual,"databaseReader").matches(expected));
    }
}
