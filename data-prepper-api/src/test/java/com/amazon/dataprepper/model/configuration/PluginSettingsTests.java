package com.amazon.dataprepper.model.configuration;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class PluginSettingsTests {
    private static final String TEST_PLUGIN_NAME = "test";

    private static final String TEST_STRING_DEFAULT_VALUE = "DEFAULT";

    private static final int TEST_INT_DEFAULT_VALUE = 1000;
    private static final int TEST_INT_VALUE = TEST_INT_DEFAULT_VALUE + 1;

    private static final boolean TEST_BOOL_DEFAULT_VALUE = Boolean.FALSE;
    private static final boolean TEST_BOOL_VALUE = !TEST_BOOL_DEFAULT_VALUE;

    private static final long TEST_LONG_DEFAULT_VALUE = 1000L;
    private static final long TEST_LONG_VALUE = TEST_LONG_DEFAULT_VALUE + 1;

    private static final String TEST_INT_ATTRIBUTE = "int-attribute";
    private static final String TEST_STRING_ATTRIBUTE = "string-attribute";
    private static final String TEST_BOOL_ATTRIBUTE = "bool-attribute";
    private static final String TEST_LONG_ATTRIBUTE = "long-attribute";


    @Test
    public void testPluginSetting() {
        final String TEST_PIPELINE = "test-pipeline";
        final int TEST_WORKERS = 1;

        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, ImmutableMap.of());
        pluginSetting.setPipelineName(TEST_PIPELINE);
        pluginSetting.setProcessWorkers(TEST_WORKERS);
        assertThat(pluginSetting, notNullValue());
        assertThat(pluginSetting.getName(), is(TEST_PLUGIN_NAME));
        assertThat(pluginSetting.getPipelineName(), is(TEST_PIPELINE));
        assertThat(pluginSetting.getNumberOfProcessWorkers(), is(TEST_WORKERS));
    }

    @Test
    public void testPluginSettingAttributes() {
        final String TEST_STRING_VALUE = "TEST";

        final Map<String, Object> TEST_SETTINGS = ImmutableMap.of(
                TEST_INT_ATTRIBUTE, TEST_INT_VALUE,
                TEST_STRING_ATTRIBUTE, TEST_STRING_VALUE,
                TEST_BOOL_ATTRIBUTE, TEST_BOOL_VALUE,
                TEST_LONG_ATTRIBUTE, TEST_LONG_VALUE);

        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, TEST_SETTINGS);

        assertThat(pluginSetting.getAttributeFromSettings(TEST_INT_ATTRIBUTE), is(TEST_INT_VALUE));

        // test attributes that exist when passing in a different default value
        assertThat(pluginSetting.getAttributeOrDefault(TEST_INT_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), is(TEST_INT_VALUE));
        assertThat(pluginSetting.getIntegerOrDefault(TEST_INT_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), is(TEST_INT_VALUE));
        assertThat(pluginSetting.getStringOrDefault(TEST_STRING_ATTRIBUTE, TEST_STRING_DEFAULT_VALUE),
                is(equalTo(TEST_STRING_VALUE)));
        assertThat(pluginSetting.getBooleanOrDefault(TEST_BOOL_ATTRIBUTE, TEST_BOOL_DEFAULT_VALUE), is(equalTo(TEST_BOOL_VALUE)));
        assertThat(pluginSetting.getLongOrDefault(TEST_LONG_ATTRIBUTE, TEST_LONG_DEFAULT_VALUE), is(equalTo(TEST_LONG_VALUE)));
    }

    @Test
    public void testPluginSettingAttributesAsString() {
        final String TEST_INT_VALUE_STRING = String.valueOf(TEST_INT_VALUE);
        final String TEST_BOOL_VALUE_STRING = String.valueOf(TEST_BOOL_VALUE);
        final String TEST_LONG_VALUE_STRING = String.valueOf(TEST_LONG_VALUE);

        final String TEST_INT_STRING_ATTRIBUTE = "int-string-attribute";
        final String TEST_BOOL_STRING_ATTRIBUTE = "bool-string-attribute";
        final String TEST_LONG_STRING_ATTRIBUTE = "long-string-attribute";

        final Map<String, Object> TEST_SETTINGS_AS_STRINGS = ImmutableMap.of(
                TEST_INT_STRING_ATTRIBUTE, TEST_INT_VALUE_STRING,
                TEST_BOOL_STRING_ATTRIBUTE, TEST_BOOL_VALUE_STRING,
                TEST_LONG_STRING_ATTRIBUTE, TEST_LONG_VALUE_STRING);


        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, TEST_SETTINGS_AS_STRINGS);

        // test attributes that exist when passing in a different default value
        assertThat(pluginSetting.getIntegerOrDefault(TEST_INT_STRING_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), is(TEST_INT_VALUE));
        assertThat(pluginSetting.getBooleanOrDefault(TEST_BOOL_STRING_ATTRIBUTE, TEST_BOOL_DEFAULT_VALUE), is(equalTo(TEST_BOOL_VALUE)));
        assertThat(pluginSetting.getLongOrDefault(TEST_LONG_STRING_ATTRIBUTE, TEST_LONG_DEFAULT_VALUE), is(equalTo(TEST_LONG_VALUE)));
    }

    /**
     * Request attributes are present with null values, expect nulls to be returned
     */
    @Test
    public void testPluginSettingAttributesAsNull() {
        final String TEST_INT_NULL_ATTRIBUTE = "int-null-attribute";
        final String TEST_STRING_NULL_ATTRIBUTE = "string-null-attribute";
        final String TEST_BOOL_NULL_ATTRIBUTE = "bool-null-attribute";
        final String TEST_LONG_NULL_ATTRIBUTE = "long-null-attribute";

        final Map<String, Object> TEST_SETTINGS_AS_STRINGS = new HashMap<>();
        TEST_SETTINGS_AS_STRINGS.put(TEST_INT_NULL_ATTRIBUTE, null);
        TEST_SETTINGS_AS_STRINGS.put(TEST_STRING_NULL_ATTRIBUTE, null);
        TEST_SETTINGS_AS_STRINGS.put(TEST_BOOL_NULL_ATTRIBUTE, null);
        TEST_SETTINGS_AS_STRINGS.put(TEST_LONG_NULL_ATTRIBUTE, null);

        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, TEST_SETTINGS_AS_STRINGS);

        // test attributes that exist when passing in a different default value
        assertThat(pluginSetting.getIntegerOrDefault(TEST_INT_NULL_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), nullValue());
        assertThat(pluginSetting.getStringOrDefault(TEST_STRING_NULL_ATTRIBUTE, TEST_STRING_DEFAULT_VALUE), nullValue());
        assertThat(pluginSetting.getBooleanOrDefault(TEST_BOOL_NULL_ATTRIBUTE, TEST_BOOL_DEFAULT_VALUE), nullValue());
        assertThat(pluginSetting.getLongOrDefault(TEST_LONG_NULL_ATTRIBUTE, TEST_LONG_DEFAULT_VALUE), nullValue());
    }

    /**
     * Requested attributes are not present, expect default values to be returned
     */
    @Test
    public void testPluginSettingAttributesNotPresent() {
        final String NOT_PRESENT_ATTRIBUTE = "not-present";

        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, null);

        assertThat(pluginSetting.getSettings(), nullValue());
        assertThat(pluginSetting.getAttributeFromSettings(NOT_PRESENT_ATTRIBUTE), nullValue());

        assertThat(pluginSetting.getAttributeOrDefault(NOT_PRESENT_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), is(TEST_INT_DEFAULT_VALUE));
        assertThat(pluginSetting.getIntegerOrDefault(NOT_PRESENT_ATTRIBUTE, TEST_INT_DEFAULT_VALUE), is(TEST_INT_DEFAULT_VALUE));
        assertThat(pluginSetting.getStringOrDefault(NOT_PRESENT_ATTRIBUTE, TEST_STRING_DEFAULT_VALUE),
                is(equalTo(TEST_STRING_DEFAULT_VALUE)));
        assertThat(pluginSetting.getBooleanOrDefault(NOT_PRESENT_ATTRIBUTE, TEST_BOOL_DEFAULT_VALUE), is(equalTo(TEST_BOOL_DEFAULT_VALUE)));
        assertThat(pluginSetting.getLongOrDefault(NOT_PRESENT_ATTRIBUTE, TEST_LONG_DEFAULT_VALUE), is(equalTo(TEST_LONG_DEFAULT_VALUE)));
    }

    @Test
    public void testPluginSettingAttributeUnsupportedType() {
        final Object UNSUPPORTED_TYPE = new ArrayList<>();

        final Map<String, Object> TEST_SETTINGS_AS_STRINGS = new HashMap<>();
        TEST_SETTINGS_AS_STRINGS.put(TEST_INT_ATTRIBUTE, UNSUPPORTED_TYPE);
        TEST_SETTINGS_AS_STRINGS.put(TEST_STRING_ATTRIBUTE, UNSUPPORTED_TYPE);
        TEST_SETTINGS_AS_STRINGS.put(TEST_BOOL_ATTRIBUTE, UNSUPPORTED_TYPE);
        TEST_SETTINGS_AS_STRINGS.put(TEST_LONG_ATTRIBUTE, UNSUPPORTED_TYPE);

        final PluginSetting pluginSetting = new PluginSetting(TEST_PLUGIN_NAME, TEST_SETTINGS_AS_STRINGS);

        // test attributes that exist when passing in a different default value
        assertThrows(IllegalArgumentException.class, () -> pluginSetting.getIntegerOrDefault(TEST_INT_ATTRIBUTE, TEST_INT_DEFAULT_VALUE));
        assertThrows(IllegalArgumentException.class, () -> pluginSetting.getStringOrDefault(TEST_STRING_ATTRIBUTE, TEST_STRING_DEFAULT_VALUE));
        assertThrows(IllegalArgumentException.class, () -> pluginSetting.getBooleanOrDefault(TEST_BOOL_ATTRIBUTE, TEST_BOOL_DEFAULT_VALUE));
        assertThrows(IllegalArgumentException.class, () -> pluginSetting.getLongOrDefault(TEST_LONG_ATTRIBUTE, TEST_LONG_DEFAULT_VALUE));
    }
}
