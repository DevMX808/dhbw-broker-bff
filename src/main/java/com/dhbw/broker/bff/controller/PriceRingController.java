package com.dhbw.broker.bff.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dhbw.broker.bff.domain.Asset;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.service.GraphqlPriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceRingController {

    private final GraphqlPriceService priceService;
    private final AssetRepository assetRepository;

    @GetMapping("/24h/{assetSymbol}")
    public ResponseEntity<?> get24hPrices(@PathVariable String assetSymbol) {
        if (assetSymbol == null || assetSymbol.isBlank() || assetSymbol.length() > 10) {
            return ResponseEntity.badRequest().body("assetSymbol invalid");
        }

        var prices = priceService.get24hPrices(assetSymbol);
        return prices.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(prices);
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<Map<String, Object>>> getMarketSymbols() {
        List<Asset> activeAssets = assetRepository.findAllActive();

        List<Map<String, Object>> symbols = activeAssets.stream()
                .map(asset -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("symbol", asset.getAssetSymbol());
                    map.put("name", asset.getName());
                    map.put("minTradeIncrement", asset.getMinTradeIncrement());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(symbols);
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<Map<String, Object>> getQuote(@PathVariable String symbol) {
        if (symbol == null || symbol.isBlank() || symbol.length() > 10) {
            return ResponseEntity.badRequest().build();
        }

        var assetOpt = assetRepository.findByAssetSymbolAndActive(symbol);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Asset asset = assetOpt.get();
        BigDecimal currentPrice = priceService.getCurrentPrice(symbol);

        if (currentPrice == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> quote = Map.of(
                "symbol", asset.getAssetSymbol(),
                "name", asset.getName(),
                "price", currentPrice,
                "updatedAt", OffsetDateTime.now().toString(),
                "updatedAtReadable", OffsetDateTime.now().toString()
        );

        return ResponseEntity.ok(quote);
    }

    @GetMapping("/trend/{symbol}")
    public ResponseEntity<Map<String, String>> getPriceTrend(@PathVariable String symbol) {
        if (symbol == null || symbol.isBlank() || symbol.length() > 10) {
            return ResponseEntity.badRequest().build();
        }

        var prices = priceService.get24hPrices(symbol);

        if (prices.size() < 2) {
            return ResponseEntity.ok(Map.of("priceChange", "UNKNOWN"));
        }

        var newestPrice = new BigDecimal(prices.getLast().get("priceUsd").toString());
        var previousPrice = new BigDecimal(prices.get(prices.size() - 2).get("priceUsd").toString());

        String priceChange = newestPrice.compareTo(previousPrice) > 0 ? "UP" :
                newestPrice.compareTo(previousPrice) < 0 ? "DOWN" : "SAME";

        return ResponseEntity.ok(Map.of("priceChange", priceChange));
    }
}