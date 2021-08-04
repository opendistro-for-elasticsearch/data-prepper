package com.amazon.dataprepper.plugins.prepper.geoip;

import java.util.Map;
import java.util.Optional;

public class IpParser {
    public IpParser() {
    }

    /**
     * Currently simply takes a hashmap of Key Value pairs and returns the target field.
     * TODO: Enable parsing of a nested IP Address?
     * TODO: Validation of IP Address before sending
     *
     * @param Data        The json data in hashmap format.
     * @param targetField The field in which to extract the data.
     * @return String with the IP Address from the targetField.
     */
    public Optional<String> getIpFromJson(final Map<String, Object> Data, final String targetField) {
        return Optional.ofNullable((String) Data.get(targetField));
        //TODO Check this value looks like an IP address before sending.
    }
}
