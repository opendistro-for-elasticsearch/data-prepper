package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.plugins.prepper.geoip.GeoIpPrepperConfig;
import com.maxmind.db.CHMCache;
import com.maxmind.db.InvalidDatabaseException;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;


public class MaxMindGeoIpProvider implements GeoIpProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MaxMindGeoIpProvider.class);
    private final DatabaseReader databaseReader;

    public MaxMindGeoIpProvider(String databasePath) {

        Objects.requireNonNull(databasePath, String.format("Missing '%s' configuration value", GeoIpPrepperConfig.DATABASE_PATH));
        File database = new File(databasePath);
        try {
            databaseReader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
        } catch (InvalidDatabaseException e) {
            throw new IllegalArgumentException("The database provided is invalid or corrupted.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("The database provided was not found in the path", e);
        }
    }

    @Override
    public final Optional<LocationData> getDataFromIp(String IpAddress) {
        try {
            final InetAddress ipAddress = InetAddress.getByName(IpAddress);
            final CityResponse response = databaseReader.city(ipAddress);
            //TODO change what kind of data is returned, allowing users to submit desired fields, etc.
            final Country country = response.getCountry();
            final Subdivision subdivision = response.getMostSpecificSubdivision();
            final City city = response.getCity();
            final LocationData returnData = new LocationData(country.getName(), subdivision.getName(), city.getName());
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
