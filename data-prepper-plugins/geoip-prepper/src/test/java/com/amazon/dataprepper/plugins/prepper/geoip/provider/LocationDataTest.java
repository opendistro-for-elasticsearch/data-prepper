package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationDataTest {
    private static final String TEST_TOSTRING_OUTPUT = "LocationData{countryName='a', subdivisionName='b', cityName='c'}";
    private static LocationData expectedLocation;

    @BeforeEach
    public void setup() {
        expectedLocation = new LocationData("a", "b", "c");

    }

    @Test
    public void testToString() {
        Assertions.assertEquals(TEST_TOSTRING_OUTPUT, expectedLocation.toString());
    }

    @Test
    public void testEquals() {
        LocationData equalLocation = new LocationData("a", "b", "c");
        LocationData unequalLocation = new LocationData("a", "b", "f");

        Assertions.assertEquals(expectedLocation, expectedLocation);
        Assertions.assertNotEquals(expectedLocation, null);
        Assertions.assertNotEquals(expectedLocation, new Object());
        Assertions.assertNotEquals(expectedLocation, unequalLocation);
        Assertions.assertEquals(expectedLocation, equalLocation);
        Assertions.assertEquals(expectedLocation.hashCode(), equalLocation.hashCode());
    }
}
