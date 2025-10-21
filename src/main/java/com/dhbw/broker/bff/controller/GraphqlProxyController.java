package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/graphql")
public class GraphqlProxyController {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final URI upstream;
    private final String upstreamAudience;
    private final String upstreamScope;

    public GraphqlProxyController(
            RestTemplate restTemplate,
            JwtService jwtService,
            @Value("${app.upstream.graphql-url}") String upstreamUrl,
            @Value("${app.upstream.graphql-audience:graphql}") String upstreamAudience,
            @Value("${app.upstream.graphql-scope:graphql:proxy}") String upstreamScope
    ) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.upstream = URI.create(upstreamUrl);
        this.upstreamAudience = upstreamAudience;
        this.upstreamScope = upstreamScope;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> proxy(@RequestBody String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String svcJwt = jwtService.issueServiceToken(
                "graphql", List.of("graphql:proxy")
        );
        headers.setBearerAuth(svcJwt);

        HttpEntity<String> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(upstream, req, String.class);

        HttpHeaders out = new HttpHeaders();
        if (resp.getHeaders().getContentType() != null) {
            out.setContentType(resp.getHeaders().getContentType());
        }
        return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
    }
}