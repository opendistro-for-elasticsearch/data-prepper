package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIPPrepperConfig;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.HashMap;


public class MaxMindGeoIpProviderTest {
    static PluginSetting pluginSetting;
    private static final String PROVIDER_TYPE = "MaxMindGeolite2CityDatabase";
    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";
    private static final String TEST_BAD_DATABASE_PATH = "./src/test/resources/Corrupted-City-Test.mmdb";
    private static final String TEST_WRONG_DATABASE_PATH = "./does/not/exist/GeoLite2-City-Test.mmdb";

    public static MaxMindGeoIpProvider maxMindGeoIpProvider;

    @BeforeAll
    public static void setup() {
        pluginSetting = new PluginSetting(
                "geoip",
                new HashMap<String, Object>() {{
                    put(GeoIPPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                    put(GeoIPPrepperConfig.DATA_SOURCE, PROVIDER_TYPE);
                }}
        );
        maxMindGeoIpProvider = new MaxMindGeoIpProvider(pluginSetting);
    }

    @Test
    public void testIpLookup() {
        LocationData locationData = new LocationData("United Kingdom", "West Berkshire", "Boxford");
        Assert.assertTrue(new ReflectionEquals(locationData).matches(maxMindGeoIpProvider.getDataFromIp("2.125.160.216")));
    }

    @Test
    public void missingIpLookup() {
        Assert.assertTrue(maxMindGeoIpProvider.getDataFromIp("43.34.246.154") == null);
    }
    @Test
    public void badIpLookup() {
        Assert.assertTrue(maxMindGeoIpProvider.getDataFromIp("GARBAGE") == null);
    }
    @Test
    public void badDatabase() {
        pluginSetting = new PluginSetting(
                "geoip",
                new HashMap<String, Object>() {{
                    put(GeoIPPrepperConfig.DATABASE_PATH, TEST_BAD_DATABASE_PATH);
                }}
        );

        Assert.assertThrows(IllegalArgumentException.class,() -> new MaxMindGeoIpProvider(pluginSetting));
    }

    @Test
    public void wrongDatabase() {
        pluginSetting = new PluginSetting(
                "geoip",
                new HashMap<String, Object>() {{
                    put(GeoIPPrepperConfig.DATABASE_PATH, TEST_WRONG_DATABASE_PATH);
                }}
        );

        Assert.assertThrows(IllegalArgumentException.class,() -> new MaxMindGeoIpProvider(pluginSetting));
    }
    //TODO Write tests for database not found, corrupted database
}
