package com.amazon.dataprepper.plugins.prepper.geoip;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class IpParserTest {

    private static final String TEST_DOCUMENT_IP_FIELD_PRESENT_JSON_FILE = "sample-document-with-ip.json";
    private static final String IP_FIELD = "peer_ip";
    private static final String MISSING_FIELD = "client_ip";
    private static final String DESIRED_IP = "127.0.0.1";
    public static IpParser ipParser;
    public Map<String, Object> document;

    @BeforeEach
    public void setup() throws IOException {
        ipParser = new IpParser();
        document = buildDocumentFromJsonFile(TEST_DOCUMENT_IP_FIELD_PRESENT_JSON_FILE);
    }
    @Test
    public void testIpParser() {
        Assert.assertTrue(ipParser.getIpFromJSON(document, IP_FIELD).equals(DESIRED_IP));
    }
    @Test
    public void testIpParserMissingField() {
        Assert.assertTrue(ipParser.getIpFromJSON(document, MISSING_FIELD) == null);
    }

    private Map<String, Object> buildDocumentFromJsonFile(String jsonFileName) throws IOException{
        final StringBuilder jsonBuilder = new StringBuilder();
        try (final InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(jsonFileName))){
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.lines().forEach(jsonBuilder::append);
        }
        final String documentJson = jsonBuilder.toString();
        Map<String, Object> response = new ObjectMapper().readValue(documentJson, HashMap.class);
        return response;

    }
}
