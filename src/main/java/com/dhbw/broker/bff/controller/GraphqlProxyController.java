package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/graphql")
public class GraphqlProxyController {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final URI upstream;

    public GraphqlProxyController(RestTemplate restTemplate,
                                  JwtService jwtService,
                                  @Value("${app.upstream.graphql-url}") String upstreamUrl) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.upstream = URI.create(upstreamUrl);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> proxy(@RequestBody String body,
                                        Authentication authentication) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) authentication;
        Jwt in = auth.getToken();

        UUID userId = UUID.fromString(in.getSubject());
        String email = in.getClaimAsString("email");
        String given = in.getClaimAsString("given_name");
        String family = in.getClaimAsString("family_name");
        List<String> roles = in.getClaimAsStringList("roles");
        boolean isAdmin = roles != null && roles.contains("ADMIN");

        String upstreamToken = jwtService
                .issueUpstreamToken(userId, email, given, family, isAdmin)
                .value();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(upstreamToken);

        HttpEntity<String> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(upstream, req, String.class);

        HttpHeaders out = new HttpHeaders();
        out.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
    }
}