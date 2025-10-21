package com.dhbw.broker.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String privateKeyPem,
        String keyId,
        String issuer,
        String privateKeyPemB64
) {}


