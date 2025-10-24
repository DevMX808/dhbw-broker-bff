package com.dhbw.broker.bff.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/me")
    public Map<String, Object> me(@org.springframework.security.core.annotation.AuthenticationPrincipal
                                  org.springframework.security.oauth2.jwt.Jwt jwt) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("id", jwt.getSubject());
        out.put("email", jwt.getClaimAsString("email"));
        var name = jwt.getClaim("name");
        if (name instanceof java.util.Map<?,?> m) {
            out.put("firstName", String.valueOf(m.get("given_name")));
            out.put("lastName", String.valueOf(m.get("family_name")));
        }
        out.put("roles", jwt.getClaimAsStringList("roles"));
        out.put("exp", jwt.getExpiresAt());
        return out;
    }
}