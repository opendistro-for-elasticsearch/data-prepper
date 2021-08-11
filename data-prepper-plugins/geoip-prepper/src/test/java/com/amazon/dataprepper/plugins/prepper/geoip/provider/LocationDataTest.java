package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;

public class LocationDataTest {
    private static LocationData expectedLocation;

    @BeforeEach
    public void setup() {
        expectedLocation = new LocationData(new HashMap<GeoDataField, Object>() {{
            put(GeoDataField.COUNTRY_NAME, "a");
            put(GeoDataField.REGION_NAME, "b");
            put(GeoDataField.CITY_NAME, "c");
        }});

    }


    @Test
    public void testEquals() {
        LocationData equalLocation = new LocationData(new HashMap<GeoDataField, Object>() {{
            put(GeoDataField.COUNTRY_NAME, "a");
            put(GeoDataField.REGION_NAME, "b");
            put(GeoDataField.CITY_NAME, "c");
        }});
        LocationData unequalLocation = new LocationData(new HashMap<GeoDataField, Object>() {{
            put(GeoDataField.COUNTRY_NAME, "d");
            put(GeoDataField.REGION_NAME, "e");
            put(GeoDataField.CITY_NAME, "f");
        }});

        Assertions.assertEquals(expectedLocation, expectedLocation);
        Assertions.assertNotEquals(expectedLocation, null);
        Assertions.assertNotEquals(expectedLocation, new Object());
        Assertions.assertNotEquals(expectedLocation, unequalLocation);
        Assertions.assertEquals(expectedLocation, equalLocation);
        Assertions.assertEquals(expectedLocation.hashCode(), equalLocation.hashCode());
    }
}
