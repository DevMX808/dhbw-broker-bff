package com.dhbw.broker.bff.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class JwkConfig {

    @Bean
    public RSAKey rsaSigningKey() throws Exception {
        return new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }

    @Bean
    public JWKSet jwkSet(RSAKey rsaSigningKey) {
        return new JWKSet(rsaSigningKey.toPublicJWK());
    }
}