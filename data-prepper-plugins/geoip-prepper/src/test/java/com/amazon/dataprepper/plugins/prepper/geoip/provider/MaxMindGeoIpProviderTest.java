package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MaxMindGeoIpProviderTest {
    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";
    private static final String TEST_BAD_DATABASE_PATH = "./src/test/resources/Corrupted-City-Test.mmdb";
    private static final String TEST_WRONG_DATABASE_PATH = "./does/not/exist/GeoLite2-City-Test.mmdb";
    public static MaxMindGeoIpProvider maxMindGeoIpProvider;

    @BeforeAll
    public static void setup() {
        maxMindGeoIpProvider = new MaxMindGeoIpProvider(TEST_DATABASE_PATH);
    }

    @Test
    public void testIpLookup() {
        LocationData locationData = new LocationData("United Kingdom", "West Berkshire", "Boxford");
        Assertions.assertEquals(locationData, maxMindGeoIpProvider.getDataFromIp("2.125.160.216").orElse(null));
    }

    @Test
    public void missingIpLookup() {
        Assertions.assertFalse(maxMindGeoIpProvider.getDataFromIp("43.34.246.154").isPresent());
    }

    @Test
    public void badIpLookup() {
        Assertions.assertFalse(maxMindGeoIpProvider.getDataFromIp("GARBAGE").isPresent());
    }

    @Test
    public void badDatabase() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new MaxMindGeoIpProvider(TEST_BAD_DATABASE_PATH));
    }

    @Test
    public void wrongDatabase() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new MaxMindGeoIpProvider(TEST_WRONG_DATABASE_PATH));
    }
}
