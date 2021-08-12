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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;


public class MaxMindGeoIpProvider implements GeoIpProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MaxMindGeoIpProvider.class);
    private final DatabaseReader databaseReader;
    private final List<GeoDataField> desiredFields = new ArrayList<>();

    public MaxMindGeoIpProvider(String databasePath, String[] desiredFields) {
        Objects.requireNonNull(databasePath, String.format("Missing '%s' configuration value", GeoIpPrepperConfig.DATABASE_PATH));
        File database = new File(databasePath);
        if (desiredFields != null && desiredFields.length > 0) {
            for (String desiredField : desiredFields) {
                try {
                    GeoDataField field = GeoDataField.valueOf(desiredField.toUpperCase(Locale.ROOT));
                    this.desiredFields.add(field);
                } catch (Exception e) {
                    LOG.error("Invalid field in config settings, {}", desiredField);
                }
            }
        } else {
            this.desiredFields.addAll(Arrays.asList(GeoDataField.values()));
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
            final LocationData.Builder builder = new LocationData.Builder();
            for (GeoDataField desiredField : desiredFields) {
                switch (desiredField) {
                    case IP:
                        builder.withIp(ipAddress.getHostAddress());
                        break;
                    case CITY_NAME:
                        builder.withCityName(city.getName());
                        break;
                    case REGION_NAME:
                        builder.withRegionName(subdivision.getName());
                        break;
                    case COUNTRY_NAME:
                        builder.withCountry(country.getName());
                        break;
                    case CONTINENT_CODE:
                        builder.withContinent(continent.getCode());
                        break;
                    case COUNTRY_ISO_CODE:
                        builder.withCountryCode(country.getIsoCode());
                        break;
                    case REGION_CODE:
                        builder.withRegionCode(subdivision.getIsoCode());
                        break;
                    case POSTAL_CODE:
                        builder.withPostalCode(postal.getCode());
                        break;
                    case TIMEZONE:
                        builder.withTimeZone(location.getTimeZone());
                        break;
                    case LOCATION:
                        builder.withLocation(new Double[]{location.getLatitude(), location.getLongitude()});
                        break;
                    case LATITUDE:
                        builder.withLatitude(location.getLatitude());
                        break;
                    case LONGITUDE:
                        builder.withLongitude(location.getLongitude());
                        break;
                }
            }
            final LocationData returnData = builder.build();
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
