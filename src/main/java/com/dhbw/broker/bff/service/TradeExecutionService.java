package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.*;
import com.dhbw.broker.bff.dto.TradeMessage;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.repository.HeldTradeRepository;
import com.dhbw.broker.bff.repository.TradeRepository;
import com.dhbw.broker.bff.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service für die eigentliche Trade-Ausführung (wird vom Queue Listener aufgerufen)
 */
@Service
public class TradeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(TradeExecutionService.class);

    private final TradeRepository tradeRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final GraphqlPriceService priceService;
    private final WalletService walletService;
    private final HeldTradeRepository heldTradeRepository;

    public TradeExecutionService(
            TradeRepository tradeRepository,
            AssetRepository assetRepository,
            UserRepository userRepository,
            GraphqlPriceService priceService,
            WalletService walletService,
            HeldTradeRepository heldTradeRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.priceService = priceService;
        this.walletService = walletService;
        this.heldTradeRepository = heldTradeRepository;
    }

    @Transactional
    public void executeTradeFromQueue(TradeMessage message) {
        logger.info("Processing trade from queue: {} {} {} for user {}",
                message.side(), message.quantity(), message.assetSymbol(), message.userEmail());

        User user = userRepository.findById(message.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Asset asset = assetRepository.findByAssetSymbolAndActive(message.assetSymbol())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or inactive asset: " + message.assetSymbol()));

        TradeSide tradeSide;
        try {
            tradeSide = TradeSide.valueOf(message.side().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade side");
        }

        if (message.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        if (asset.getMinTradeIncrement() != null &&
                message.quantity().remainder(asset.getMinTradeIncrement()).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be a multiple of " + asset.getMinTradeIncrement());
        }

        BigDecimal currentPrice = priceService.getCurrentPrice(message.assetSymbol());
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to get current price for asset: " + message.assetSymbol());
        }

        BigDecimal tradeValue = currentPrice.multiply(message.quantity());

        if (tradeSide == TradeSide.BUY) {
            BigDecimal balance = walletService.getCurrentBalance(user.getUserId());
            if (balance.compareTo(tradeValue) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nicht genug Guthaben für diesen Kauf");
            }
            walletService.deductForTrade(user, tradeValue,
                    "BUY " + message.quantity() + " " + message.assetSymbol() + " @ $" + currentPrice);
        } else if (tradeSide == TradeSide.SELL) {
            HeldTrade heldTrade = heldTradeRepository.findByUserIdAndAssetSymbol(user.getUserId(), asset.getAssetSymbol())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Nicht genug Assets zum Verkaufen"));
            if (heldTrade.getQuantity().compareTo(message.quantity()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nicht genug Assets zum Verkaufen");
            }
            walletService.addFromSale(user, tradeValue,
                    "SELL " + message.quantity() + " " + message.assetSymbol() + " @ $" + currentPrice);
        }

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setAsset(asset);
        trade.setSide(tradeSide);
        trade.setQuantity(message.quantity());
        trade.setPriceUsd(currentPrice);
        trade.setExecutedAt(OffsetDateTime.now());

        Trade savedTrade = tradeRepository.save(trade);

        updateHeldTrades(user, asset, tradeSide, message.quantity(), currentPrice, savedTrade.getTradeId());

        logger.info("Trade executed successfully from queue: {} {} {} at ${} for user {}",
                tradeSide, message.quantity(), message.assetSymbol(), currentPrice, user.getEmail());
    }

    private void updateHeldTrades(User user, Asset asset, TradeSide tradeSide,
                                  BigDecimal quantity, BigDecimal price, UUID tradeId) {

        List<HeldTrade> existingForAsset = heldTradeRepository.findAllByUserIdAndAssetSymbol(user.getUserId(), asset.getAssetSymbol());
        HeldTrade target = null;
        if (existingForAsset != null && !existingForAsset.isEmpty()) {
            target = existingForAsset.getFirst();
            if (existingForAsset.size() > 1) {
                BigDecimal totalQuantity = target.getQuantity();
                BigDecimal totalValue = target.getBuyPriceUsd().multiply(target.getQuantity());
                for (int i = 1; i < existingForAsset.size(); i++) {
                    HeldTrade dup = existingForAsset.get(i);
                    totalQuantity = totalQuantity.add(dup.getQuantity());
                    totalValue = totalValue.add(dup.getBuyPriceUsd().multiply(dup.getQuantity()));
                    heldTradeRepository.delete(dup);
                }
                BigDecimal mergedAvg = totalValue.divide(totalQuantity, 8, RoundingMode.HALF_UP);
                target.setQuantity(totalQuantity);
                target.setBuyPriceUsd(mergedAvg);
                heldTradeRepository.save(target);
            }
        }

        if (tradeSide == TradeSide.BUY) {
            if (target != null) {
                BigDecimal newQuantity = target.getQuantity().add(quantity);
                BigDecimal totalValue = target.getBuyPriceUsd().multiply(target.getQuantity())
                        .add(price.multiply(quantity));
                BigDecimal avgPrice = totalValue.divide(newQuantity, 8, RoundingMode.HALF_UP);

                target.setQuantity(newQuantity);
                target.setBuyPriceUsd(avgPrice);
                heldTradeRepository.save(target);
            } else {
                HeldTrade newHeldTrade = new HeldTrade();
                newHeldTrade.setUserId(user.getUserId());
                newHeldTrade.setTradeId(System.currentTimeMillis());
                newHeldTrade.setAssetSymbol(asset.getAssetSymbol());
                newHeldTrade.setQuantity(quantity);
                newHeldTrade.setBuyPriceUsd(price);
                newHeldTrade.setCreatedAt(OffsetDateTime.now());
                heldTradeRepository.save(newHeldTrade);
            }
        } else if (tradeSide == TradeSide.SELL) {
            if (target != null) {
                BigDecimal newQuantity = target.getQuantity().subtract(quantity);
                if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    heldTradeRepository.delete(target);
                } else {
                    target.setQuantity(newQuantity);
                    heldTradeRepository.save(target);
                }
            }
        }
    }
}