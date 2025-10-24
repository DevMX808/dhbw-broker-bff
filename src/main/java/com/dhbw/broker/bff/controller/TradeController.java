package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.*;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.service.GraphqlPriceService;
import com.dhbw.broker.bff.service.IdentityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final IdentityService identityService;
    private final GraphqlPriceService priceService;

    public TradeController(IdentityService identityService, GraphqlPriceService priceService) {
        this.identityService = identityService;
        this.priceService = priceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ResponseEntity<?> executeTrade(@RequestBody TradeRequest request) {
        try {
            // Get current user
            User user = identityService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Validate asset
            Asset asset = entityManager.find(Asset.class, request.getAssetSymbol());
            if (asset == null || !asset.isActive()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or inactive asset"));
            }

            // Validate trade side
            TradeSide tradeSide;
            try {
                tradeSide = TradeSide.valueOf(request.getSide().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid trade side. Must be BUY or SELL"));
            }

            // Validate quantity
            if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be positive"));
            }

            if (asset.getMinTradeIncrement() != null && 
                request.getQuantity().remainder(asset.getMinTradeIncrement()).compareTo(BigDecimal.ZERO) != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantity must be a multiple of " + asset.getMinTradeIncrement()
                ));
            }

            // Get current price
            BigDecimal currentPrice = priceService.getCurrentPrice(request.getAssetSymbol());
            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unable to get current price for asset"));
            }

            // Create trade
            Trade trade = new Trade();
            trade.setUser(user);
            trade.setAsset(asset);
            trade.setSide(tradeSide);
            trade.setQuantity(request.getQuantity());
            trade.setPriceUsd(currentPrice);
            trade.setExecutedAt(OffsetDateTime.now());

            // Save trade
            entityManager.persist(trade);
            entityManager.flush();

            Map<String, Object> response = new HashMap<>();
            response.put("tradeId", trade.getTradeId());
            response.put("executedAt", trade.getExecutedAt());
            response.put("priceUsd", trade.getPriceUsd());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error executing trade", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserTrades() {
        try {
            User user = identityService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            List<Trade> trades = entityManager.createQuery(
                    "SELECT t FROM Trade t JOIN FETCH t.asset WHERE t.user = :user ORDER BY t.executedAt DESC",
                    Trade.class)
                    .setParameter("user", user)
                    .setMaxResults(100)
                    .getResultList();

            return ResponseEntity.ok(trades);
            
        } catch (Exception e) {
            logger.error("Error fetching user trades", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
}