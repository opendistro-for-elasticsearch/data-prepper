package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationDataTest {
    private static LocationData expectedLocation;

    @BeforeEach
    public void setup() {
        expectedLocation = new LocationData.Builder().withCountry("a").withRegionName("b").withCityName("c").build();


    }

    @Test
    public void testEquals() {
        LocationData equalLocation = new LocationData.Builder().withCountry("a").withRegionName("b").withCityName("c").build();
        LocationData unequalLocation = new LocationData.Builder().withCountry("d").withRegionName("e").withCityName("f").build();

        Assertions.assertEquals(expectedLocation, expectedLocation);
        Assertions.assertNotEquals(expectedLocation, null);
        Assertions.assertNotEquals(expectedLocation, new Object());
        Assertions.assertNotEquals(expectedLocation, unequalLocation);
        Assertions.assertEquals(expectedLocation, equalLocation);
        Assertions.assertEquals(expectedLocation.hashCode(), equalLocation.hashCode());
    }
}
