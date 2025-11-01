package com.dhbw.broker.bff.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URI;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

public final class AlexaSignatureVerifier {

    private static final List<String> ALLOWED_HOSTS = List.of(
            "s3.amazonaws.com",
            "localhost",
            "127.0.0.1",
            "rocky-atoll-88358-b10b362cee67.herokuapp.com"
    );

    private AlexaSignatureVerifier() {
    }

    public static boolean isSignatureValid(String signatureBase64, String certUrl, byte[] bodyBytes) {
        try {
            if (!isCertUrlAllowed(certUrl)) {
                return false;
            }

            URI uri = URI.create(certUrl);
            HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
            connection.connect();

            try (InputStream in = connection.getInputStream()) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
                cert.checkValidity();

                PublicKey publicKey = cert.getPublicKey();

                byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

                Signature sig = Signature.getInstance("SHA1withRSA");
                sig.initVerify(publicKey);
                sig.update(bodyBytes);

                return sig.verify(signatureBytes);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTimestampValid(String timestampIso) {
        if (timestampIso == null || timestampIso.isBlank()) {
            return false;
        }
        try {
            Instant ts = Instant.parse(timestampIso);
            Instant now = Instant.now();
            long diff = Math.abs(ChronoUnit.SECONDS.between(now, ts));
            return diff <= 150;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCertUrlAllowed(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            for (String allowed : ALLOWED_HOSTS) {
                if (host.equalsIgnoreCase(allowed) || host.toLowerCase().endsWith("." + allowed.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
