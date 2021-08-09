package com.amazon.dataprepper.plugins.prepper.geoip;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.Fields;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.GeoIpProvider;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.LocationData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeoIpPrepperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEST_PIPELINE_NAME = "testPipelineName";
    private static final String PLUGIN_NAME = "geoip_prepper";
    private static final String PROVIDER_TYPE = "MaxMindGeolite2CityDatabase";
    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";
    private static final String TEST_RAW_SPAN_INVALID_IP_JSON_FILE = "raw-span-invalid-ip.json";
    private static final String TEST_RAW_SPAN_VAILD_IP_JSON_FILE = "raw-span-valid-ip.json";
    private static final String TEST_SPAN_TARGET_FIELD = "resource.attributes.client@ip";
    private static final String TEST_SPAN_BAD_TARGET_FIELD = "doesnt.exist.client@ip";
    private static final String TEST_LOCATION_FIELD_NAME = "locationData";
    private static final String[] TEST_DESIRED_FIELDS = {"city_name", "country_name", "region_name"};


    private static final LocationData TEST_LOCATION_DATA = new LocationData(new HashMap<Fields, Object>() {{
        put(Fields.COUNTRY_NAME, "United Kingdom");
        put(Fields.REGION_NAME, "West Berkshire");
        put(Fields.CITY_NAME, "Boxford");
    }});
    final PluginSetting testPluginSetting = new PluginSetting(
            PLUGIN_NAME,
            new HashMap<String, Object>() {{
                put(GeoIpPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                put(GeoIpPrepperConfig.TARGET_FIELD, TEST_SPAN_TARGET_FIELD);
                put(GeoIpPrepperConfig.DATA_SOURCE, PROVIDER_TYPE);
                put(GeoIpPrepperConfig.DESIRED_FIELDS, TEST_DESIRED_FIELDS);

            }}
    ) {{
        setPipelineName(TEST_PIPELINE_NAME);
    }};
    @Mock
    private GeoIpProvider testProvider;
    private GeoIpPrepper geoIpPrepper;

    @BeforeEach
    public void setUp() {
        geoIpPrepper = new GeoIpPrepper(testPluginSetting, testProvider);
    }

    @AfterEach
    public void tearDown() {
        geoIpPrepper.shutdown();
    }

    @Test
    public void testPrepareForShutdown() {
        geoIpPrepper.prepareForShutdown();

        Assertions.assertTrue(geoIpPrepper.isReadyForShutdown());
    }

    @Test
    public void testSuccessfulIpLookup() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_VAILD_IP_JSON_FILE);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        when(testProvider.getDataFromIp(any())).thenReturn(Optional.of(TEST_LOCATION_DATA));

        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);

        Assertions.assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertEquals(TEST_LOCATION_DATA.toMap().get(Fields.CITY_NAME.fieldName()), rawSpanMap.get(Fields.CITY_NAME.fieldName()));
        Assertions.assertEquals(TEST_LOCATION_DATA.toMap().get(Fields.REGION_NAME.fieldName()), rawSpanMap.get(Fields.REGION_NAME.fieldName()));
        Assertions.assertEquals(TEST_LOCATION_DATA.toMap().get(Fields.COUNTRY_NAME.fieldName()), rawSpanMap.get(Fields.COUNTRY_NAME.fieldName()));
    }

    @Test
    public void testUnsuccessfulIpLookup() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_INVALID_IP_JSON_FILE);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        when(testProvider.getDataFromIp(any())).thenReturn(Optional.empty());

        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);

        Assertions.assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertNull(rawSpanMap.get(TEST_LOCATION_FIELD_NAME));
    }

    @Test
    public void testBadRecord() {
        geoIpPrepper = new GeoIpPrepper(testPluginSetting);
        Record<String> testRecord = new Record<>("[],");
        List<Record<String>> testRecords = Collections.singletonList(testRecord);

        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);

        Assertions.assertEquals(0, recordsOut.size());
    }

    @Test
    public void testTargetFieldMissing() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_INVALID_IP_JSON_FILE);
        final PluginSetting testPluginSetting = new PluginSetting(
                PLUGIN_NAME,
                new HashMap<String, Object>() {{
                    put(GeoIpPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                    put(GeoIpPrepperConfig.TARGET_FIELD, TEST_SPAN_BAD_TARGET_FIELD);
                }}
        ) {{
            setPipelineName(TEST_PIPELINE_NAME);
        }};
        geoIpPrepper = new GeoIpPrepper(testPluginSetting, testProvider);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);

        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);

        Assertions.assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertNull(rawSpanMap.get(TEST_LOCATION_FIELD_NAME));
    }

    private Record<String> buildRawSpanRecord(String rawSpanJsonFileName) throws IOException {
        final StringBuilder jsonBuilder = new StringBuilder();
        try (final InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(rawSpanJsonFileName))) {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.lines().forEach(jsonBuilder::append);
        }
        return new Record<>(jsonBuilder.toString());
    }

}
