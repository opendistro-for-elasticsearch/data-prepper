package com.amazon.dataprepper.plugins.certificate;

import com.amazon.dataprepper.plugins.certificate.model.Certificate;
import com.amazon.dataprepper.plugins.source.oteltrace.OTelTraceSourceConfig;

public interface CertificateProvider {
    Certificate getCertificate(final OTelTraceSourceConfig oTelTraceSourceConfig);
}
