package com.dhbw.broker.bff.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class FrontendConfigController {

    @GetMapping("/frontend-config")
    public ResponseEntity<Map<String, Object>> config() {
        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        Map<String, Object> body = Map.of(
                "bffBaseUrl", baseUrl,
                "apiBasePath", "/api",
                "auth", Map.of("login", "/auth/login", "register", "/auth/register")
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(body);
    }
}