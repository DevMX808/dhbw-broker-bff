package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        } catch (Exception e) {
            logger.error("Error executing trade: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Trade execution failed");
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
        } catch (Exception e) {
            logger.error("Error fetching user trades: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch trades");
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
        } catch (Exception e) {
            logger.error("Error fetching user trades for asset {}: {}", assetSymbol, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch trades");
        }
    }
}