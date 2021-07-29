package com.amazon.dataprepper.plugins.prepper.geoip;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.plugins.prepper.geoip.provider.LocationData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class GeoIPPrepperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GeoIPPrepper geoIpPrepper;
    private static final String TEST_PIPELINE_NAME = "testPipelineName";
    private static final String PLUGIN_NAME = "geoip_prepper";
    private static final String PROVIDER_TYPE = "MaxMindGeolite2CityDatabase";
    private static final String TEST_DATABASE_PATH = "./src/test/resources/GeoLite2-City-Test.mmdb";
    private static final String TEST_RAW_SPAN_INVALID_IP_JSON_FILE = "raw-span-invalid-ip.json";
    private static final String TEST_RAW_SPAN_VAILD_IP_JSON_FILE = "raw-span-valid-ip.json";
    private static final String TEST_SPAN_TARGET_FIELD = "resource.attributes.client@ip";
    private static final String TEST_SPAN_BAD_TARGET_FIELD = "doesnt.exist.client@ip";
    private static final String TEST_LOCATION_FIELD_NAME = "Location Data";

    private static final LocationData TEST_LOCATION_DATA = new LocationData("United Kingdom", "West Berkshire", "Boxford");

    @BeforeEach
    public void setUp(){
        GeoIPPrepperConfig testConfig = new GeoIPPrepperConfig();
        final PluginSetting testPluginSetting = new PluginSetting(
                PLUGIN_NAME,
                new HashMap<String, Object>() {{
                    put(testConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                    put(testConfig.DATA_SOURCE, PROVIDER_TYPE);
                    put(testConfig.TARGET_FIELD, TEST_SPAN_TARGET_FIELD);
                }}
        ){{
            setPipelineName(TEST_PIPELINE_NAME);
        }};
        geoIpPrepper = new GeoIPPrepper(testPluginSetting);
    }

    @AfterEach
    public void tearDown() {
        geoIpPrepper.shutdown();
    }

    @Test
    public void testPrepareForShutdown() {
        geoIpPrepper.prepareForShutdown();

        assertTrue(geoIpPrepper.isReadyForShutdown());
    }
    @Test
    public void testSuccessfulIpLookup() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_VAILD_IP_JSON_FILE);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);
        assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {});

        assertEquals(TEST_LOCATION_DATA.toString(), rawSpanMap.get(TEST_LOCATION_FIELD_NAME));

    }

    @Test
    public void testUnsuccessfulIpLookup() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_INVALID_IP_JSON_FILE);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);
        assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {});
        assertEquals(null, rawSpanMap.get(TEST_LOCATION_FIELD_NAME));
    }
    @Test
    public void testBadRecord() throws IOException {
        Record<String> testRecord = new Record<>("[],");
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);
        assertEquals(0, recordsOut.size());
    }

    @Test
    public void testTargetFieldMissing() throws IOException {
        Record<String> testRecord = buildRawSpanRecord(TEST_RAW_SPAN_INVALID_IP_JSON_FILE);
        final PluginSetting testPluginSetting = new PluginSetting(
                PLUGIN_NAME,
                new HashMap<String, Object>() {{
                    put(GeoIPPrepperConfig.DATABASE_PATH, TEST_DATABASE_PATH);
                    put(GeoIPPrepperConfig.DATA_SOURCE, PROVIDER_TYPE);
                    put(GeoIPPrepperConfig.TARGET_FIELD, TEST_SPAN_BAD_TARGET_FIELD);
                }}
        ){{
            setPipelineName(TEST_PIPELINE_NAME);
        }};
        geoIpPrepper = new GeoIPPrepper(testPluginSetting);
        List<Record<String>> testRecords = Collections.singletonList(testRecord);
        List<Record<String>> recordsOut = (List<Record<String>>) geoIpPrepper.doExecute(testRecords);
        assertEquals(1, recordsOut.size());
        Record<String> recordOut = recordsOut.get(0);
        Map<String, Object> rawSpanMap = OBJECT_MAPPER.readValue(recordOut.getData(), new TypeReference<Map<String, Object>>() {});
        assertEquals(null, rawSpanMap.get(TEST_LOCATION_FIELD_NAME));
    }

    private Record<String> buildRawSpanRecord(String rawSpanJsonFileName) throws IOException {
        final StringBuilder jsonBuilder = new StringBuilder();
        try (final InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(rawSpanJsonFileName))){
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.lines().forEach(jsonBuilder::append);
        }
        return new Record<>(jsonBuilder.toString());
    }

}
