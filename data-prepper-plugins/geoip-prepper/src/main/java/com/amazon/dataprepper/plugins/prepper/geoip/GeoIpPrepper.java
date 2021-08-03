package com.amazon.dataprepper.plugins.prepper.geoip;

import com.amazon.dataprepper.model.PluginType;
import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.prepper.AbstractPrepper;
import com.amazon.dataprepper.model.record.Record;
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
import java.util.Objects;
import java.util.Optional;


@DataPrepperPlugin(name = "geoip_prepper", type = PluginType.PREPPER)
public class GeoIpPrepper extends AbstractPrepper<Record<String>, Record<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(GeoIpPrepper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private final String targetField;
    private final GeoIpProvider provider;
    private final IpParser parser;
    private final String locationField;

    public GeoIpPrepper(final PluginSetting pluginSetting) {
        super(pluginSetting);
        Objects.requireNonNull(pluginSetting);
        targetField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.TARGET_FIELD, null);
        locationField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.LOCATION_FIELD, GeoIpPrepperConfig.DEFAULT_LOCATION_FIELD);
        provider = new GeoIpProviderFactory().createGeoIpProvider(pluginSetting);
        parser = new IpParser();
    }
    //
    GeoIpPrepper(final PluginSetting pluginSetting, final GeoIpProvider testProvider) {
        // accessible only in the same package for unit test
        super(pluginSetting);
        provider = testProvider;
        parser = new IpParser();
        targetField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.TARGET_FIELD, null);
        locationField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.LOCATION_FIELD, GeoIpPrepperConfig.DEFAULT_LOCATION_FIELD);
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
        for (Record<String> record : rawSpanStringRecords) {
            try {
                final Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(record.getData(), MAP_TYPE_REFERENCE);

                final Optional<String> foundIp = parser.getIpFromJson(rawSpanMap, targetField);

                if (foundIp.isPresent()) {
                    final Optional<LocationData> foundData = provider.getDataFromIp(foundIp.get());
                    //TODO This is a placeholder, how data is attached is still TBD
                    foundData.ifPresent(locationData -> rawSpanMap.put(locationField, locationData.toString()));
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