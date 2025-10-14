package com.dhbw.broker.bff.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class JwksController {
    private final JWKSet jwkSet;
    private final ObjectMapper objectMapper;
    @Autowired
    public JwksController(JWKSet jwkSet, ObjectMapper objectMapper) {
        this.jwkSet = jwkSet;
        this.objectMapper = objectMapper;
    }
    @GetMapping(value = "/jwks.json", produces = "application/json")
    public String getJwks() throws Exception {
        return objectMapper.writeValueAsString(jwkSet.toJSONObject());
    }
}