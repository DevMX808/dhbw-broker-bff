package com.dhbw.broker.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class FrontendConfigController {

    @GetMapping("/frontend-config")
    public Map<String, Object> config(HttpServletRequest req) {
        String scheme = Optional.ofNullable(req.getHeader("X-Forwarded-Proto"))
                .orElse(req.getScheme());

        String host = Optional.ofNullable(req.getHeader("X-Forwarded-Host"))
                .orElse(null);

        if (host == null || host.isBlank()) {
            int port = req.getServerPort();
            boolean defaultPort = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
            host = req.getServerName() + (defaultPort ? "" : ":" + port);
        }

        String prefix = Optional.ofNullable(req.getHeader("X-Forwarded-Prefix")).orElse("");

        String baseUrl = scheme + "://" + host + prefix;

        return Map.of(
                "bffBaseUrl", baseUrl,
                "apiBasePath", "/api",
                "auth", Map.of("login", "/auth/login", "register", "/auth/register")
        );
    }
}