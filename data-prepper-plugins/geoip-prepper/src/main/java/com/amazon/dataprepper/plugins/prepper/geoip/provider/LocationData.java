package com.amazon.dataprepper.plugins.prepper.geoip.provider;


// This is a first pass of LocationData, simply to make sure lookup correctly passes through data.
//TODO Find the best way to attach the location data to a document, and modify this class to allow that.

public class LocationData {
    public String countryName;
    public String subdivisionName;
    public String cityName;

    public LocationData(String country, String subdivision, String city){
        countryName = country;
        subdivisionName = subdivision;
        cityName = city;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "countryName='" + countryName + '\'' +
                ", subdivisionName='" + subdivisionName + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}
