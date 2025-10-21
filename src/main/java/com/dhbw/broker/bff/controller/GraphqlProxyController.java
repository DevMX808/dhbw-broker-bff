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
    public ResponseEntity<String> proxy(@RequestBody String body, Authentication authentication) {

        JwtAuthenticationToken auth = (JwtAuthenticationToken) authentication;
        Jwt jwt = auth.getToken();
        UUID userId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");

        String upstreamJwt = jwtService.issueGraphqlUpstreamToken(userId, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(upstreamJwt);

        HttpEntity<String> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(upstream, req, String.class);

        HttpHeaders out = new HttpHeaders();
        MediaType ct = resp.getHeaders().getContentType();
        if (ct != null) out.setContentType(ct);

        return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
    }
}