package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.Asset;
import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.repository.HeldTradeRepository;
import com.dhbw.broker.bff.service.GraphqlPriceService;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.service.TradeService;
import com.dhbw.broker.bff.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final IdentityService identityService;
    private final GraphqlPriceService priceService;
    private final WalletService walletService;
    private final AssetRepository assetRepository;
    private final HeldTradeRepository heldTradeRepository;

    @PostMapping
    public ResponseEntity<?> executeTrade(@RequestBody TradeRequest request) {
        if (request == null || request.getAssetSymbol() == null || request.getAssetSymbol().isBlank()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        try {
            validateTradeBeforeQueuing(request);

            var tradeResponse = tradeService.executeTrade(request);
            return ResponseEntity.ok(tradeResponse);
        } catch (ResponseStatusException e) {
            logger.warn("Trade execution failed for user: {} - {}", e.getReason(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Unexpected error executing trade", e);
            return ResponseEntity.internalServerError().body("Trade execution failed due to server error");
        }
    }

    private void validateTradeBeforeQueuing(TradeRequest request) {
        var user = identityService.getCurrentUser();
        UUID userId = user.getUserId();

        Asset asset = assetRepository.findByAssetSymbolAndActive(request.getAssetSymbol())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or inactive asset: " + request.getAssetSymbol()));

        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        if (asset.getMinTradeIncrement() != null &&
                request.getQuantity().remainder(asset.getMinTradeIncrement()).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be a multiple of " + asset.getMinTradeIncrement());
        }

        if ("BUY".equalsIgnoreCase(request.getSide())) {
            BigDecimal approximatePrice = priceService.getCurrentPrice(request.getAssetSymbol());
            if (approximatePrice == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to get current price for asset: " + request.getAssetSymbol());
            }

            BigDecimal approximateValue = approximatePrice.multiply(request.getQuantity());
            BigDecimal balance = walletService.getCurrentBalance(userId);

            if (balance.compareTo(approximateValue) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Nicht genug Guthaben für diesen Kauf. Benötigt: ~$%.2f USD, Verfügbar: $%.2f USD",
                                approximateValue, balance));
            }
            logger.info("Pre-validation passed: User {} has sufficient balance (${}) for approximate trade value (${})",
                    user.getEmail(), balance, approximateValue);
        } else if ("SELL".equalsIgnoreCase(request.getSide())) {
            HeldTrade heldTrade = heldTradeRepository.findByUserIdAndAssetSymbol(userId, asset.getAssetSymbol())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Nicht genug Assets zum Verkaufen"));

            if (heldTrade.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Nicht genug Assets zum Verkaufen. Verfügbar: %.4f %s, Gewünscht: %.4f %s",
                                heldTrade.getQuantity(), asset.getAssetSymbol(),
                                request.getQuantity(), asset.getAssetSymbol()));
            }
            logger.info("Pre-validation passed: User {} has sufficient holdings ({}) for sell quantity ({})",
                    user.getEmail(), heldTrade.getQuantity(), request.getQuantity());
        }
    }

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