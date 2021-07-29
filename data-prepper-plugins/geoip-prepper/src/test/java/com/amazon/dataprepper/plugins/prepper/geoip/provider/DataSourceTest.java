package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
    private static final String TEST_TOSTRING_OUTPUT = "LocationData{countryName=\'a\', subdivisionName=\'b\', cityName=\'c\'}";
    @Test
    public void testToString(){
        LocationData actual = new LocationData("a", "b", "c");
        Assert.assertTrue(actual.toString().equals(TEST_TOSTRING_OUTPUT));
    }
}
