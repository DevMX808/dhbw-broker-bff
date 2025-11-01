package com.dhbw.broker.bff.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public final class AlexaSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(AlexaSignatureVerifier.class);
    private static final long MAX_AGE_SECONDS = 150L;

    private AlexaSignatureVerifier() {
    }

    public static boolean isTimestampValid(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return false;
        }
        try {
            Instant requestTs = Instant.parse(isoTimestamp);
            Instant now = Instant.now();
            long diff = Math.abs(ChronoUnit.SECONDS.between(now, requestTs));
            return diff <= MAX_AGE_SECONDS;
        } catch (Exception e) {
            log.warn("Could not parse Alexa timestamp: {}", isoTimestamp);
            return false;
        }
    }

    public static boolean isSignatureValid(String signatureB64, String certUrl, byte[] rawBody) {
        try {
            if (signatureB64 == null || certUrl == null || rawBody == null) {
                log.warn("Missing signature, certUrl or body");
                return false;
            }
            if (!isValidCertUrl(certUrl)) {
                log.warn("Invalid Alexa cert URL: {}", certUrl);
                return false;
            }
            X509Certificate cert = downloadCertificate(certUrl);
            if (cert == null) {
                log.warn("Could not download Alexa certificate");
                return false;
            }
            cert.checkValidity();
            byte[] signatureBytes = Base64.getDecoder().decode(signatureB64);
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(cert.getPublicKey());
            sig.update(rawBody);
            boolean ok = sig.verify(signatureBytes);
            if (!ok) {
                log.warn("Alexa signature verification failed");
            }
            return ok;
        } catch (Exception e) {
            log.warn("Alexa signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private static boolean isValidCertUrl(String certUrl) {
        try {
            URI uri = new URI(certUrl);
            URL url = uri.toURL();
            if (!"https".equalsIgnoreCase(url.getProtocol())) {
                return false;
            }
            if (!"s3.amazonaws.com".equalsIgnoreCase(url.getHost())) {
                return false;
            }
            return url.getPath().startsWith("/echo.api/");
        } catch (Exception e) {
            return false;
        }
    }

    private static X509Certificate downloadCertificate(String certUrl) throws Exception {
        URI uri = new URI(certUrl);
        URL url = uri.toURL();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        try (InputStream in = conn.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(in);
        }
    }
}
