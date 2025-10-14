package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    private final RSAKey rsaKey;
    private final String issuer;
    private final Duration ttl;
    private Instant lastExp;

    public JwtService(RSAKey rsaKey,
                      @Value("${security.jwt.issuer:dhbw-broker-bff}") String issuer,
                      @Value("${security.jwt.ttl-seconds:3600}") long ttlSeconds) {
        this.rsaKey = rsaKey;
        this.issuer = issuer;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        lastExp = exp;

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId() != null ? user.getId().toString() : UUID.randomUUID().toString())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("name", Map.of("given_name", user.getFirstName(), "family_name", user.getLastName()))
                .claim("roles", user.isAdmin() ? new String[]{"ADMIN"} : new String[]{"USER"})
                .build();

        try {
            var signer = new com.nimbusds.jose.crypto.RSASSASigner(rsaKey);
            var header = new com.nimbusds.jose.JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID())
                    .build();
            var jwt = new com.nimbusds.jwt.SignedJWT(header, claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Konnte JWT nicht signieren", e);
        }
    }

    public Instant getExpiresAt() { return lastExp; }
}