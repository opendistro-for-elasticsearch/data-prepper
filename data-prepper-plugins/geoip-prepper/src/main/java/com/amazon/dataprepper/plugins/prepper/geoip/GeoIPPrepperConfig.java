package com.amazon.dataprepper.plugins.prepper.geoip;

import com.amazon.dataprepper.plugins.prepper.geoip.provider.DataSource;

public final class GeoIPPrepperConfig {
    //TODO add config variables for cache size, location fields, possibly more
    public static final String TARGET_FIELD = "target_field";
    public static final String DATA_SOURCE = "data_source";
    public static final String DEFAULT_DATA_SOURCE = DataSource.MaxMindGeolite2CityDatabase.toString();
    public static final String DATABASE_PATH = "database_path";
}
