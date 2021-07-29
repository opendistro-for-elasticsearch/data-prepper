package com.amazon.dataprepper.plugins.prepper.geoip;

import java.util.Map;

public class IpParser {
    public IpParser() {
    }

    /**
     * Currently simply takes a hashmap of Key Value pairs and returns the target field.
     * TODO: Enable parsing of a nested IP Address?
     * TODO: Validation of IP Address before sending
     * @param Data
     * @param targetField
     * @return String with the IP Address from the targetField.
     */
    public String getIpFromJSON(Map<String, Object> Data, String targetField){

        return (String) Data.get(targetField);
        //TODO Check this value looks like an IP address before sending.
    }
}
