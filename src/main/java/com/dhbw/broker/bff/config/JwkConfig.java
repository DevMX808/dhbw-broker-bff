package com.dhbw.broker.bff.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class JwkConfig {

    @Bean
    public RSAKey rsaSigningKey(JwtProperties props) throws Exception {
        String pem = props.privateKeyPem();
        if ((pem == null || pem.isBlank()) && props.privateKeyPemB64() != null) {
            pem = new String(Base64.getDecoder().decode(props.privateKeyPemB64()), StandardCharsets.UTF_8);
        }
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException("Missing jwt.private-key-pem (oder jwt.private-key-pem-b64)");
        }

        JWK jwk = JWK.parseFromPEMEncodedObjects(pem);

        if (!(jwk instanceof RSAKey rsaParsed)) {
            throw new IllegalArgumentException("Provided PEM is no RSA Key");
        }

        return new RSAKey.Builder(rsaParsed.toRSAPublicKey())
                .privateKey(rsaParsed.toPrivateKey())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID((props.keyId() == null || props.keyId().isBlank()) ? "local" : props.keyId())
                .build();
    }

    @Bean
    public JWKSet jwkSet(RSAKey rsaSigningKey) {
        return new JWKSet(rsaSigningKey.toPublicJWK());
    }
}