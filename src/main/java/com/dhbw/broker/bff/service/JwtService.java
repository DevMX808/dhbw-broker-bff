package com.dhbw.broker.bff.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final RSAKey rsaSigningKey;
    private final String issuer;
    private final Duration ttl;

    public JwtService(RSAKey rsaSigningKey,
                      @Value("${security.jwt.issuer}") String issuer,
                      @Value("${security.jwt.ttl-minutes}") long ttlMinutes) {
        this.rsaSigningKey = rsaSigningKey;
        this.issuer = issuer;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public AccessToken issueAccessToken(UUID userId,
                                        String email,
                                        String firstName,
                                        String lastName,
                                        boolean isAdmin) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(ttl);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaSigningKey.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            List<String> roles = List.of(isAdmin ? "ADMIN" : "USER");

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("email", email)
                    .claim("given_name", firstName)
                    .claim("family_name", lastName)
                    .claim("roles", roles)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(rsaSigningKey);
            jwt.sign(signer);

            return new AccessToken(jwt.serialize(), exp);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }
}