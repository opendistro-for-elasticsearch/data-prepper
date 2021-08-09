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
        targetField = (String) pluginSetting.getAttributeFromSettings(GeoIpPrepperConfig.TARGET_FIELD);
        Objects.requireNonNull(targetField);
        locationField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.LOCATION_FIELD, GeoIpPrepperConfig.DEFAULT_LOCATION_FIELD);
        provider = new GeoIpProviderFactory().createGeoIpProvider(pluginSetting);
        parser = new IpParser();
    }

    //
    GeoIpPrepper(final PluginSetting pluginSetting, final GeoIpProvider geoIpProvider) {
        // accessible only in the same package for unit test
        super(pluginSetting);
        provider = geoIpProvider;
        parser = new IpParser();
        targetField = (String) pluginSetting.getAttributeFromSettings(GeoIpPrepperConfig.TARGET_FIELD);
        Objects.requireNonNull(targetField);
        locationField = pluginSetting.getStringOrDefault(GeoIpPrepperConfig.LOCATION_FIELD, GeoIpPrepperConfig.DEFAULT_LOCATION_FIELD);
    }

    /**
     * Execute the prepper logic. Records are input as rawSpans in a json string form.
     * Ip address is extracted from a target field defined in the pluginSetting, and looked up using a geo Ip provider
     * defined by the pluginSetting. Then, data is attached to the document, in a field defined by the pluginSetting.
     *
     * @param rawSpanStringRecords Input records that will be modified/processed
     * @return Record  modified output records
     */
    @Override
    public Collection<Record<String>> doExecute(final Collection<Record<String>> rawSpanStringRecords) {
        final List<Record<String>> recordsOut = new LinkedList<>();
        for (Record<String> record : rawSpanStringRecords) {
            try {
                final Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(record.getData(), MAP_TYPE_REFERENCE);

                final Optional<String> foundIp = parser.getIpFromJson(rawSpanMap, targetField);
                if (foundIp.isPresent()) {
                    final Optional<LocationData> foundData = provider.getDataFromIp(foundIp.get());
                    if (foundData.isPresent()) {
                        for (Map.Entry<String, Object> entry : foundData.get().toMap().entrySet())
                            rawSpanMap.put(entry.getKey(), entry.getValue());
                        final String newData = OBJECT_MAPPER.writeValueAsString(rawSpanMap);
                        recordsOut.add(new Record<>(newData, record.getMetadata()));
                    } else {
                        recordsOut.add(record);
                    }
                } else {
                    recordsOut.add(record);
                    //TODO Handle logging for no IP returned
                }
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