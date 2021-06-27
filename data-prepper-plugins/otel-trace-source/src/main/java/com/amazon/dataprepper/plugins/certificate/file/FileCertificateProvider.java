package com.amazon.dataprepper.plugins.certificate.file;

import com.amazon.dataprepper.plugins.certificate.CertificateProvider;
import com.amazon.dataprepper.plugins.certificate.model.Certificate;
import com.amazon.dataprepper.plugins.source.oteltrace.OTelTraceSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileCertificateProvider implements CertificateProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FileCertificateProvider.class);

    public Certificate getCertificate(final OTelTraceSourceConfig oTelTraceSourceConfig) {
        try {
            final Path certFilePath = Path.of(oTelTraceSourceConfig.getSslKeyCertChainFile());
            final Path privatedKeyFilePath = Path.of(oTelTraceSourceConfig.getSslKeyFile());

            final String certAsString = Files.readString(certFilePath);
            final String privateKeyAsString = Files.readString(privatedKeyFilePath);

            return new Certificate(certAsString, privateKeyAsString);
        } catch (final Exception ex) {
            LOG.error("Error encountered while reading the certificate.", ex);
            throw new RuntimeException(ex);
        }
    }
}
