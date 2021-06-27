package com.amazon.dataprepper.plugins.certificate.acm;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.certificate.model.Certificate;
import com.amazon.dataprepper.plugins.source.oteltrace.OTelTraceSourceConfig;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ACMCertificateProviderTest {
    @Mock
    private AWSCertificateManager  awsCertificateManager;

    @Mock
    private ExportCertificateResult exportCertificateResult;

    private ACMCertificateProvider acmCertificateProvider;

    private OTelTraceSourceConfig oTelTraceSourceConfig;

    @BeforeEach
    public void beforeEach() {
        acmCertificateProvider = new ACMCertificateProvider(awsCertificateManager);

        final Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("useAcmCertForSSL", true);
        settingsMap.put("awsRegion", "us-eas-1");
        settingsMap.put("acmCertIssueTimeOutMillis", 2000);
        settingsMap.put("acmCertificateArn", "arn:aws:acm:us-east-1:account:certificate/1234-567-856456");
        settingsMap.put("acmPrivateKeyPassword", "password");
        settingsMap.put("sslKeyCertChainFile", "data/certificate/test_cert.crt");
        settingsMap.put("sslKeyFile", "data/certificate/test_decrypted_key.key");

        final PluginSetting pluginSetting = new PluginSetting(null, settingsMap);
        pluginSetting.setPipelineName("pipeline");

        oTelTraceSourceConfig = OTelTraceSourceConfig.buildConfig(pluginSetting);
    }

    @Test
    public void getACMCertificateWithEncryptedPrivateKeySuccess() throws IOException {
        final Path certFilePath = Path.of("data/certificate/test_cert.crt");
        final Path encryptedKeyFilePath = Path.of("data/certificate/test_encrypted_key.key");
        final Path decryptedKeyFilePath = Path.of("data/certificate/test_decrypted_key.key");
        final String certAsString = Files.readString(certFilePath);
        final String encryptedKeyAsString = Files.readString(encryptedKeyFilePath);
        final String decryptedKeyAsString = Files.readString(decryptedKeyFilePath);
        when(exportCertificateResult.getCertificate()).thenReturn(certAsString);
        when(exportCertificateResult.getPrivateKey()).thenReturn(encryptedKeyAsString);
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenReturn(exportCertificateResult);
        final Certificate certificate = acmCertificateProvider.getCertificate(oTelTraceSourceConfig);
        assertThat(certificate.getCertificate(), is(certAsString));
        assertThat(certificate.getPrivateKey(), is(decryptedKeyAsString));
    }

    @Test
    public void getACMCertificateWithUnencryptedPrivateKeySuccess() throws IOException {
        final Path certFilePath = Path.of("data/certificate/test_cert.crt");
        final Path decryptedKeyFilePath = Path.of("data/certificate/test_decrypted_key.key");
        final String certAsString = Files.readString(certFilePath);
        final String decryptedKeyAsString = Files.readString(decryptedKeyFilePath);
        when(exportCertificateResult.getCertificate()).thenReturn(certAsString);
        when(exportCertificateResult.getPrivateKey()).thenReturn(decryptedKeyAsString);
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenReturn(exportCertificateResult);
        final Certificate certificate = acmCertificateProvider.getCertificate(oTelTraceSourceConfig);
        assertThat(certificate.getCertificate(), is(certAsString));
        assertThat(certificate.getPrivateKey(), is(decryptedKeyAsString));
    }

    @Test
    public void getACMCertificateWithInvalidPrivateKeyException() {
        when(exportCertificateResult.getPrivateKey()).thenReturn(UUID.randomUUID().toString());
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenReturn(exportCertificateResult);
        assertThrows(RuntimeException.class, () -> acmCertificateProvider.getCertificate(oTelTraceSourceConfig));
    }

    @Test
    public void getACMCertificateRequestInProgressException() {
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenThrow(new RequestInProgressException("Request in progress."));
        assertThrows(IllegalStateException.class, () -> acmCertificateProvider.getCertificate(oTelTraceSourceConfig));
    }

    @Test
    public void getACMCertificateResourceNotFoundException() {
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenThrow(new ResourceNotFoundException("Resource not found."));
        assertThrows(ResourceNotFoundException.class, () -> acmCertificateProvider.getCertificate(oTelTraceSourceConfig));
    }

    @Test
    public void getACMCertificateInvalidArnException() {
        when(awsCertificateManager.exportCertificate(any(ExportCertificateRequest.class))).thenThrow(new InvalidArnException("Invalid certificate arn."));
        assertThrows(InvalidArnException.class, () -> acmCertificateProvider.getCertificate(oTelTraceSourceConfig));
    }
}
