package com.amazon.dataprepper.plugins.certificate.file;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.certificate.model.Certificate;
import com.amazon.dataprepper.plugins.source.oteltrace.OTelTraceSourceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class FileCertificateProviderTest {
    private OTelTraceSourceConfig oTelTraceSourceConfig;

    private FileCertificateProvider fileCertificateProvider;

    @BeforeEach
    public void beforeEach() {
        fileCertificateProvider = new FileCertificateProvider();
    }

    @Test
    public void getCertificateValidPathSuccess() throws IOException {
        final Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("sslKeyCertChainFile", "data/certificate/test_cert.crt");
        settingsMap.put("sslKeyFile", "data/certificate/test_decrypted_key.key");

        final PluginSetting pluginSetting = new PluginSetting(null, settingsMap);
        pluginSetting.setPipelineName("pipeline");
        oTelTraceSourceConfig = OTelTraceSourceConfig.buildConfig(pluginSetting);

        final Certificate certificate = fileCertificateProvider.getCertificate(oTelTraceSourceConfig);

        final Path certFilePath = Path.of("data/certificate/test_cert.crt");
        final Path keyFilePath = Path.of("data/certificate/test_decrypted_key.key");
        final String certAsString = Files.readString(certFilePath);
        final String keyAsString = Files.readString(keyFilePath);

        assertThat(certificate.getCertificate(), is(certAsString));
        assertThat(certificate.getPrivateKey(), is(keyAsString));
    }

    @Test
    public void getCertificateInvalidPathSuccess() throws IOException {
        final Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("sslKeyCertChainFile", "path_does_not_exit/test_cert.crt");
        settingsMap.put("sslKeyFile", "path_does_not_exit/test_decrypted_key.key");

        final PluginSetting pluginSetting = new PluginSetting(null, settingsMap);
        pluginSetting.setPipelineName("pipeline");
        oTelTraceSourceConfig = OTelTraceSourceConfig.buildConfig(pluginSetting);

        Assertions.assertThrows(RuntimeException.class, () -> fileCertificateProvider.getCertificate(oTelTraceSourceConfig));
    }
}
