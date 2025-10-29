package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;

    /**
     * Führt einen Trade aus
     */
    @PostMapping
    public ResponseEntity<?> executeTrade(@RequestBody TradeRequest request) {
        if (request == null || request.getAssetSymbol() == null || request.getAssetSymbol().isBlank()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        try {
            var tradeResponse = tradeService.executeTrade(request);
            return ResponseEntity.ok(tradeResponse);
        } catch (ResponseStatusException e) {
            // Bekannte Exceptions direkt mit Status zurückgeben
            logger.warn("Trade execution failed for user: {} - {}", e.getReason(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Unerwartete Exceptions
            logger.error("Unexpected error executing trade", e);
            return ResponseEntity.internalServerError().body("Trade execution failed due to server error");
        }
    }

    /**
     * Holt alle Trades des aktuellen Users
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserTrades() {
        try {
            var trades = tradeService.getUserTrades();
            return ResponseEntity.ok(trades);
        } catch (ResponseStatusException e) {
            logger.warn("Fetching user trades failed: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Unexpected error fetching user trades", e);
            return ResponseEntity.internalServerError().body("Failed to fetch trades due to server error");
        }
    }

    /**
     * Holt alle Trades des aktuellen Users für ein bestimmtes Asset
     */
    @GetMapping("/user/{assetSymbol}")
    public ResponseEntity<?> getUserTradesByAsset(@PathVariable String assetSymbol) {
        if (assetSymbol == null || assetSymbol.isBlank() || assetSymbol.length() > 10) {
            return ResponseEntity.badRequest().body("Invalid asset symbol");
        }

        try {
            var trades = tradeService.getUserTradesByAsset(assetSymbol);
            return ResponseEntity.ok(trades);
        } catch (ResponseStatusException e) {
            logger.warn("Fetching user trades for asset {} failed: {}", assetSymbol, e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Unexpected error fetching trades for asset {}", assetSymbol, e);
            return ResponseEntity.internalServerError().body("Failed to fetch trades due to server error");
        }
    }
}
