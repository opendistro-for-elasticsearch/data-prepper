package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.plugins.prepper.geoip.GeoIpPrepperConfig;
import com.maxmind.db.CHMCache;
import com.maxmind.db.InvalidDatabaseException;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class MaxMindGeoIpProvider implements GeoIpProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MaxMindGeoIpProvider.class);
    private final DatabaseReader databaseReader;
    private ArrayList<Fields> fieldsToAdd = new ArrayList<>();

    public MaxMindGeoIpProvider(String databasePath, String[] desiredFields) {
        Objects.requireNonNull(databasePath, String.format("Missing '%s' configuration value", GeoIpPrepperConfig.DATABASE_PATH));
        File database = new File(databasePath);
        if (desiredFields != null && desiredFields.length > 0) {
            for (String desiredField : desiredFields) {
                try {
                    Fields field = Fields.valueOf(desiredField.toUpperCase(Locale.ROOT));
                    fieldsToAdd.add(field);
                } catch (Exception e) {
                    LOG.error("Invalid field in config settings, {}", desiredField);
                }
            }
        } else {
            fieldsToAdd = new ArrayList<>(Arrays.asList(Fields.IP, Fields.CITY_NAME,
                    Fields.CONTINENT_CODE, Fields.COUNTRY_NAME, Fields.COUNTRY_CODE2,
                    Fields.IP, Fields.POSTAL_CODE, Fields.REGION_NAME,
                    Fields.REGION_CODE, Fields.TIMEZONE, Fields.LOCATION, Fields.LATITUDE, Fields.LONGITUDE));
        }
        try {
            databaseReader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
        } catch (InvalidDatabaseException e) {
            throw new IllegalArgumentException("The database provided is invalid or corrupted.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("The database provided was not found in the path", e);
        }
    }

    @Override
    public final Optional<LocationData> getDataFromIp(final String IpAddress) {
        try {
            final InetAddress ipAddress = InetAddress.getByName(IpAddress);
            final CityResponse response = databaseReader.city(ipAddress);
            final Country country = response.getCountry();
            final Subdivision subdivision = response.getMostSpecificSubdivision();
            final City city = response.getCity();
            final Location location = response.getLocation();
            final Continent continent = response.getContinent();
            final Postal postal = response.getPostal();
            Map<Fields, Object> locationDetails = new HashMap<>();

            for (Fields desiredField : this.fieldsToAdd) {
                switch (desiredField) {
                    case IP:
                        locationDetails.put(Fields.IP, ipAddress.getHostAddress());
                        break;
                    case CITY_NAME:
                        String cityName = city.getName();
                        if (cityName != null) {
                            locationDetails.put(Fields.CITY_NAME, cityName);
                        }
                        break;
                    case REGION_NAME:
                        String subdivisionName = subdivision.getName();
                        if (subdivisionName != null) {
                            locationDetails.put(Fields.REGION_NAME, subdivisionName);
                        }
                        break;
                    case COUNTRY_NAME:
                        String countryName = country.getName();
                        if (countryName != null) {
                            locationDetails.put(Fields.COUNTRY_NAME, countryName);
                        }
                        break;
                    case CONTINENT_CODE:
                        String continentCode = continent.getCode();
                        if (continentCode != null) {
                            locationDetails.put(Fields.CONTINENT_CODE, continentCode);
                        }
                        break;
                    case COUNTRY_CODE2:
                        String countryCode2 = country.getIsoCode();
                        if (countryCode2 != null) {
                            locationDetails.put(Fields.COUNTRY_CODE2, countryCode2);
                        }
                        break;
                    case REGION_CODE:
                        String subdivisionCode = subdivision.getIsoCode();
                        if (subdivisionCode != null) {
                            locationDetails.put(Fields.REGION_CODE, subdivisionCode);
                        }
                        break;
                    case POSTAL_CODE:
                        String postalCode = postal.getCode();
                        if (postalCode != null) {
                            locationDetails.put(Fields.POSTAL_CODE, postalCode);
                        }
                        break;
                    case TIMEZONE:
                        String locationTimeZone = location.getTimeZone();
                        if (locationTimeZone != null) {
                            locationDetails.put(Fields.TIMEZONE, locationTimeZone);
                        }
                        break;
                    case LOCATION:
                        Double latitude = location.getLatitude();
                        Double longitude = location.getLongitude();
                        if (latitude != null && longitude != null) {
                            locationDetails.put(Fields.LOCATION, new Double[]{latitude, longitude});
                        }
                        break;
                    case LATITUDE:
                        Double lat = location.getLatitude();
                        if (lat != null) {
                            locationDetails.put(Fields.LATITUDE, lat);
                        }
                        break;
                    case LONGITUDE:
                        Double lon = location.getLongitude();
                        if (lon != null) {
                            locationDetails.put(Fields.LONGITUDE, lon);
                        }
                        break;
                }
            }
            final LocationData returnData = new LocationData(locationDetails);
            return Optional.of(returnData);
        } catch (UnknownHostException e) {
            LOG.info("IP Field contained invalid IP address or hostname.", e);
        } catch (AddressNotFoundException e) {
            LOG.info("IP not found! ", e);
        } catch (GeoIp2Exception | IOException e) {
            LOG.info("GeoIP2 Exception.", e);
        }
        return Optional.empty();
    }
}
