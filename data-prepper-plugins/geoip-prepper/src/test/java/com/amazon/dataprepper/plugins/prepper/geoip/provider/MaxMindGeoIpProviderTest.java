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
        maxMindGeoIpProvider = new MaxMindGeoIpProvider(TEST_DATABASE_PATH, null);
    }

    @Test
    public void testIpLookup() {
        LocationData.Builder builder = new LocationData.Builder();
        builder.withCountry("United Kingdom").withRegionName("West Berkshire")
                .withCityName("Boxford").withLocation(new Double[]{51.75, -1.25}).withLatitude(51.75).withLongitude(-1.25)
                .withIp("2.125.160.216").withContinent("EU").withCountryCode("GB").withRegionCode("WBK")
                .withPostalCode("OX1").withTimeZone("Europe/London");
        LocationData locationData = builder.build();
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
        Assert.assertThrows(IllegalArgumentException.class, () -> new MaxMindGeoIpProvider(TEST_BAD_DATABASE_PATH, null));
    }

    @Test
    public void wrongDatabase() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new MaxMindGeoIpProvider(TEST_WRONG_DATABASE_PATH, null));
    }

    @Test
    public void wrongFields() {
        MaxMindGeoIpProvider provider = new MaxMindGeoIpProvider(TEST_DATABASE_PATH, new String[]{"bad field"});
        Assertions.assertFalse(provider.getDataFromIp("43.34.246.154").isPresent());
    }
}
