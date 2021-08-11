package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationDataTest {
    private static LocationData expectedLocation;

    @BeforeEach
    public void setup() {
        expectedLocation = new LocationData.builder().withCountry("a").withRegion("b").withCity("c").build();


    }

    @Test
    public void testEquals() {
        LocationData equalLocation = new LocationData.builder().withCountry("a").withRegion("b").withCity("c").build();
        LocationData unequalLocation = new LocationData.builder().withCountry("d").withRegion("e").withCity("f").build();


        Assertions.assertEquals(expectedLocation, expectedLocation);
        Assertions.assertNotEquals(expectedLocation, null);
        Assertions.assertNotEquals(expectedLocation, new Object());
        Assertions.assertNotEquals(expectedLocation, unequalLocation);
        Assertions.assertEquals(expectedLocation, equalLocation);
        Assertions.assertEquals(expectedLocation.hashCode(), equalLocation.hashCode());
    }
}
