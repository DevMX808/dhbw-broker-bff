package com.dhbw.broker.bff.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

import com.dhbw.broker.bff.domain.Asset;
import com.dhbw.broker.bff.dto.SpeakableAssetPriceDto;
import com.dhbw.broker.bff.repository.AssetRepository;
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
    private final AssetRepository assetRepository;

    @Value("${app.upstream.graphql-url}")
    private String graphqlUrl;

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

    public List<SpeakableAssetPriceDto> getCurrentPricesForActiveAssets() {
        List<Asset> activeAssets = assetRepository.findAllActive();
        List<SpeakableAssetPriceDto> result = new ArrayList<>();

        for (Asset asset : activeAssets) {
            String symbol = asset.getAssetSymbol();
            BigDecimal price = getCurrentPrice(symbol);

            if (price == null) {
                log.debug("No current price for active asset {}", symbol);
                continue;
            }

            SpeakableAssetPriceDto dto = new SpeakableAssetPriceDto(
                    asset.getName(),
                    symbol,
                    price,
                    OffsetDateTime.now().toString()
            );
            result.add(dto);
        }

        return result;
    }

    public SpeakableAssetPriceDto getCurrentPriceForSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        var assetOpt = assetRepository.findByAssetSymbolAndActive(symbol);
        if (assetOpt.isEmpty()) {
            return null;
        }

        BigDecimal price = getCurrentPrice(symbol);
        if (price == null) {
            return null;
        }

        Asset asset = assetOpt.get();
        return new SpeakableAssetPriceDto(
                asset.getName(),
                asset.getAssetSymbol(),
                price,
                OffsetDateTime.now().toString()
        );
    }

    private JsonNode executeGraphqlQuery(Map<String, Object> request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

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
}
