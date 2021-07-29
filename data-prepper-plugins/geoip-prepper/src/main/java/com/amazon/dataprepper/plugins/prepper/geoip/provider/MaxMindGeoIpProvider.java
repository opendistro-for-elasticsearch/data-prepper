package com.amazon.dataprepper.plugins.prepper.geoip.provider;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.geoip.GeoIPPrepperConfig;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.db.InvalidDatabaseException;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MaxMindGeoIpProvider implements GeoIpProvider {
    private final String databasePath;
    private final DatabaseReader databaseReader;
    private final File database;
    private static final Logger LOG = LoggerFactory.getLogger(MaxMindGeoIpProvider.class);

    public MaxMindGeoIpProvider(PluginSetting pluginSetting) throws IllegalArgumentException{

        databasePath = pluginSetting.getStringOrDefault(GeoIPPrepperConfig.DATABASE_PATH, null);
        Objects.requireNonNull(databasePath, String.format("Missing '%s' configuration value", GeoIPPrepperConfig.DATABASE_PATH));
        database = new File(databasePath);
        try {
            databaseReader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
        } catch (InvalidDatabaseException e) {
            throw new IllegalArgumentException("The database provided is invalid or corrupted.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("The database provided was not found in the path", e);
        }
    }

    @Override
    public LocationData getDataFromIp(String IpAddress) {
        try {
            final InetAddress ipAddress = InetAddress.getByName(IpAddress);
            CityResponse response = databaseReader.city(ipAddress);
            //TODO change what kind of data is returned, allowing users to submit desired fields, etc.
            Country country = response.getCountry();
            Subdivision subdivision = response.getMostSpecificSubdivision();
            City city = response.getCity();
            LocationData returnData = new LocationData(country.getName(), subdivision.getName(), city.getName());
            return returnData;
        } catch (UnknownHostException e) {
            LOG.debug("IP Field contained invalid IP address or hostname. exception={}", e);
        } catch (AddressNotFoundException e) {
            LOG.debug("IP not found! exception={}", e);
        } catch (GeoIp2Exception | IOException e) {
            LOG.debug("GeoIP2 Exception. exception={}", e);
        }
        return null;
    }
}
