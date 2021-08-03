package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationDataTest {
    private static final String TEST_TOSTRING_OUTPUT = "LocationData{countryName='a', subdivisionName='b', cityName='c'}";
    private static LocationData location;

    @BeforeEach
    public void setup() {
        location = new LocationData("a", "b", "c");

    }

    @Test
    public void testToString() {
        Assertions.assertEquals(TEST_TOSTRING_OUTPUT, location.toString());
    }

    @Test
    public void testEquals() {
        LocationData location2 = new LocationData("a", "b", "c");
        LocationData location3 = new LocationData("a", "b", "f");

        Assertions.assertEquals(location, location);
        Assertions.assertNotEquals(location, null);
        Assertions.assertNotEquals(location, new Object());
        Assertions.assertNotEquals(location, location3);
        Assertions.assertEquals(location, location2);
        Assertions.assertEquals(location.hashCode(), location2.hashCode());
    }
}
