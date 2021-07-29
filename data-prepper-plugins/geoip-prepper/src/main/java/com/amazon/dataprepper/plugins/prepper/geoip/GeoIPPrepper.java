package com.amazon.dataprepper.plugins.prepper.geoip;

import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.model.PluginType;
import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.prepper.AbstractPrepper;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.GeoIpProvider;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.GeoIpProviderFactory;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.LocationData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@DataPrepperPlugin(name = "geoip_prepper", type = PluginType.PREPPER)
public class GeoIPPrepper extends AbstractPrepper<Record<String>, Record<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(GeoIPPrepper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};
    private final String targetField;
    private final GeoIpProvider provider;

    public GeoIPPrepper(final PluginSetting pluginSetting) {
        super(pluginSetting);
        targetField = pluginSetting.getStringOrDefault(GeoIPPrepperConfig.TARGET_FIELD, null);
        provider = new GeoIpProviderFactory().createGeoIpProvider(pluginSetting);
    }

    /**
     * execute the prepper logic which could potentially modify the incoming record. The level to which the record has
     * been modified depends on the implementation
     *
     * @param rawSpanStringRecords Input records that will be modified/processed
     * @return Record  modified output records
     */
    @Override
    public Collection<Record<String>> doExecute(Collection<Record<String>> rawSpanStringRecords) {
        final List<Record<String>> recordsOut = new LinkedList<>();
        IpParser parser = new IpParser();
        for (Record<String> record: rawSpanStringRecords) {
            try {
                final Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(record.getData(), MAP_TYPE_REFERENCE);

                final String foundIp = parser.getIpFromJSON(rawSpanMap, targetField);

                if(foundIp != null) {
                    final LocationData foundData = provider.getDataFromIp(foundIp);
                    if(foundData != null){
                        rawSpanMap.put("Location Data", foundData.toString());
                        //TODO This is a placeholder, how data is attached is still TBD
                    }
                } else {
                    //TODO Handle no IP returned
                }
                final String newData = OBJECT_MAPPER.writeValueAsString(rawSpanMap);
                recordsOut.add(new Record<>(newData, record.getMetadata()));
            } catch (JsonProcessingException e) {
                LOG.error("Failed to parse the record: [{}]", record.getData());
            }
        }
        return recordsOut;
    }


    //TODO implement these functions?

    @Override
    public void prepareForShutdown() {

    }

    @Override
    public boolean isReadyForShutdown() {
        return true;
    }

    @Override
    public void shutdown() {
    }
}