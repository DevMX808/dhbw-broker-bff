package com.dhbw.broker.bff.controller;

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
    private final URI upstream;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GraphqlProxyController.class);

    public GraphqlProxyController(RestTemplate restTemplate,
                                  @Value("${app.upstream.graphql-url}") String upstreamUrl) {
        this.restTemplate = restTemplate;
        this.upstream = URI.create(upstreamUrl);
        log.info("GraphQL upstream set to: {}", this.upstream);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> proxy(
            @RequestBody String body,
            @RequestHeader HttpHeaders incoming
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String auth = incoming.getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, auth);
        }

        HttpEntity<String> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp =
                restTemplate.postForEntity(upstream, req, String.class);

        HttpHeaders out = new HttpHeaders();
        if (resp.getHeaders().getContentType() != null) {
            out.setContentType(resp.getHeaders().getContentType());
        }
        return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
    }
}