package com.amazon.dataprepper.plugins.prepper.geoip.provider;

public enum DataSource {
    // Currently, only one type of database is supported. In the future, more lookup services could be added, including API based lookups.
    MaxMindGeolite2CityDatabase,
    MaxMindApiCityDatabase, // Unsupported, for testing
}
