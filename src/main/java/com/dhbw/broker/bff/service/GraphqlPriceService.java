package com.dhbw.broker.bff.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphqlPriceService {

  private static final Logger log = LoggerFactory.getLogger(GraphqlPriceService.class);

  private final RestTemplate restTemplate;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Value("${app.upstream.graphql-url}")
  private String graphqlUrl;

  /**
   * Holt alle Preispunkte der letzten 24h für Chart-Darstellung
   */
  public List<Map<String, Object>> get24hPrices(String assetSymbol) {
    String query = """
      query Get24hPrices($symbol: String!) {
        priceHistory24h(assetSymbol: $symbol) {
          assetSymbol
          slot
          priceUsd
          sourceTsUtc
          ingestedTsUtc
          isCarry
        }
      }
      """;

    Map<String, Object> variables = Map.of("symbol", assetSymbol);
    Map<String, Object> request = Map.of(
      "query", query,
      "variables", variables
    );

    try {
      JsonNode response = executeGraphqlQuery(request);
      JsonNode data = response.path("data").path("priceHistory24h");
      
      if (data.isMissingNode() || data.isNull() || !data.isArray()) {
        return List.of();
      }

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> result = objectMapper.convertValue(data, List.class);
      return result;
    } catch (Exception e) {
      log.error("Failed to get 24h prices for {}: {}", assetSymbol, e.getMessage());
      throw new RuntimeException("Failed to fetch price data", e);
    }
  }

  private JsonNode executeGraphqlQuery(Map<String, Object> request) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    // Service Token für GraphQL-Aufruf generieren
    String serviceToken = jwtService.issueServiceToken("graphql", List.of("graphql:proxy"));
    headers.setBearerAuth(serviceToken);

    String requestBody = objectMapper.writeValueAsString(request);
    HttpEntity<String> httpRequest = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(
      URI.create(graphqlUrl), 
      httpRequest, 
      String.class
    );

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("GraphQL request failed with status: " + response.getStatusCode());
    }

    return objectMapper.readTree(response.getBody());
  }

  /**
   * Holt den aktuellen Preis für ein Asset
   */
  public BigDecimal getCurrentPrice(String assetSymbol) {
    String query = """
      query GetCurrentPrice($symbol: String!) {
        currentPrice(assetSymbol: $symbol) {
          priceUsd
        }
      }
      """;

    Map<String, Object> variables = Map.of("symbol", assetSymbol);
    Map<String, Object> request = Map.of(
      "query", query,
      "variables", variables
    );

    try {
      JsonNode response = executeGraphqlQuery(request);
      JsonNode priceData = response.path("data").path("currentPrice").path("priceUsd");
      
      if (priceData.isMissingNode() || priceData.isNull()) {
        return null;
      }

      return new BigDecimal(priceData.asText());
    } catch (Exception e) {
      log.error("Failed to get current price for {}: {}", assetSymbol, e.getMessage());
      return null;
    }
  }
}