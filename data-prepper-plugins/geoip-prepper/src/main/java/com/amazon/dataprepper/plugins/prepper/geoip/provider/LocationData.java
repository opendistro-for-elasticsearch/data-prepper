package com.amazon.dataprepper.plugins.prepper.geoip.provider;


// This is a first pass of LocationData, simply to make sure lookup correctly passes through data.
//TODO Find the best way to attach the location data to a document, and modify this class to allow that.

import java.util.Objects;

public class LocationData {
    private final String countryName;
    private final String subdivisionName;
    private final String cityName;

    public LocationData(String countryName, String subdivisionName, String cityName) {
        this.countryName = countryName;
        this.subdivisionName = subdivisionName;
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "countryName='" + countryName + '\'' +
                ", subdivisionName='" + subdivisionName + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
    //TODO Update this to use ObjectMapper to format the json data.


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationData that = (LocationData) o;
        return Objects.equals(countryName, that.countryName) && Objects.equals(subdivisionName, that.subdivisionName) && Objects.equals(cityName, that.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryName, subdivisionName, cityName);
    }
}
