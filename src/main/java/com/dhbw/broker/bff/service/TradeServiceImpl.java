package com.dhbw.broker.bff.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dhbw.broker.bff.domain.Asset;
import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.domain.Trade;
import com.dhbw.broker.bff.domain.TradeSide;
import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.dto.TradeResponse;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.repository.HeldTradeRepository;
import com.dhbw.broker.bff.repository.TradeRepository;

@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final TradeRepository tradeRepository;
    private final AssetRepository assetRepository;
    private final IdentityService identityService;
    private final GraphqlPriceService priceService;
    private final WalletService walletService;
    private final HeldTradeRepository heldTradeRepository;

    public TradeServiceImpl(TradeRepository tradeRepository,
                           AssetRepository assetRepository,
                           IdentityService identityService,
                           GraphqlPriceService priceService,
                           WalletService walletService,
                           HeldTradeRepository heldTradeRepository) {
        this.tradeRepository = tradeRepository;
        this.assetRepository = assetRepository;
        this.identityService = identityService;
        this.priceService = priceService;
        this.walletService = walletService;
        this.heldTradeRepository = heldTradeRepository;
    }

    @Override
    @Transactional
    public TradeResponse executeTrade(TradeRequest request) {
        if (request == null || request.getAssetSymbol() == null || request.getAssetSymbol().isBlank() ||
            request.getQuantity() == null || request.getSide() == null || request.getSide().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        // Get current authenticated user
        User user = identityService.getCurrentUser();

        // Validate and get asset
        Asset asset = assetRepository.findByAssetSymbolAndActive(request.getAssetSymbol())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid or inactive asset: " + request.getAssetSymbol()));

        // Validate trade side
        TradeSide tradeSide;
        try {
            tradeSide = TradeSide.valueOf(request.getSide().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade side. Must be BUY or SELL");
        }

        // Validate quantity
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        if (asset.getMinTradeIncrement() != null && 
            request.getQuantity().remainder(asset.getMinTradeIncrement()).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Quantity must be a multiple of " + asset.getMinTradeIncrement());
        }

        // Get current price via GraphQL service
        BigDecimal currentPrice = priceService.getCurrentPrice(request.getAssetSymbol());
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, 
                "Unable to get current price for asset: " + request.getAssetSymbol());
        }

        // Calculate trade value
        BigDecimal tradeValue = currentPrice.multiply(request.getQuantity());
        
        // Handle wallet transactions based on trade side
        if (tradeSide == TradeSide.BUY) {
            // For BUY: Check balance and deduct from wallet
            walletService.deductForTrade(user, tradeValue, 
                "BUY " + request.getQuantity() + " " + request.getAssetSymbol() + " @ $" + currentPrice);
        } else if (tradeSide == TradeSide.SELL) {
            // For SELL: Add proceeds to wallet
            walletService.addFromSale(user, tradeValue, 
                "SELL " + request.getQuantity() + " " + request.getAssetSymbol() + " @ $" + currentPrice);
        }

        // Create and save trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setAsset(asset);
        trade.setSide(tradeSide);
        trade.setQuantity(request.getQuantity());
        trade.setPriceUsd(currentPrice);
        trade.setExecutedAt(OffsetDateTime.now());

        Trade savedTrade = tradeRepository.save(trade);
        
        // Update held trades based on trade side
        updateHeldTrades(user, asset, tradeSide, request.getQuantity(), currentPrice, savedTrade.getTradeId());
        
        logger.info("Trade executed: {} {} {} at ${} for user {}", 
                tradeSide, request.getQuantity(), request.getAssetSymbol(), currentPrice, user.getEmail());

        return new TradeResponse(
                savedTrade.getTradeId(),
                savedTrade.getExecutedAt(),
                savedTrade.getPriceUsd(),
                savedTrade.getAsset().getAssetSymbol(),
                savedTrade.getSide().name(),
                savedTrade.getQuantity()
        );
    }

    @Override
    public List<Trade> getUserTrades() {
        User user = identityService.getCurrentUser();
        return tradeRepository.findByUserOrderByExecutedAtDesc(user);
    }

    @Override
    public List<Trade> getUserTradesByAsset(String assetSymbol) {
        User user = identityService.getCurrentUser();
        return tradeRepository.findByUserAndAssetSymbolOrderByExecutedAtDesc(user, assetSymbol);
    }

    /**
     * Updates held trades based on the executed trade
     */
    private void updateHeldTrades(User user, Asset asset, TradeSide tradeSide, 
                                BigDecimal quantity, BigDecimal price, UUID tradeId) {
        
        // Consolidate existing held trades for this user+asset (in case duplicates exist)
        List<HeldTrade> existingForAsset = heldTradeRepository.findAllByUserIdAndAssetSymbol(user.getUserId(), asset.getAssetSymbol());
        HeldTrade target = null;
        if (existingForAsset != null && !existingForAsset.isEmpty()) {
            // Merge duplicates into a single record (sum quantities and compute weighted avg price)
            target = existingForAsset.get(0);
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
                logger.info("Consolidated {} held trade duplicates for user {} asset {} -> quantity={}, avgPrice={}",
                        existingForAsset.size(), user.getEmail(), asset.getAssetSymbol(), totalQuantity, mergedAvg);
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

                logger.info("Updated held trade for user {} asset {}: quantity={}, avgPrice={}",
                           user.getEmail(), asset.getAssetSymbol(), newQuantity, avgPrice);
            } else {
                HeldTrade newHeldTrade = new HeldTrade();
                newHeldTrade.setUserId(user.getUserId());
                // Keep synthetic trade id to satisfy schema constraints
                newHeldTrade.setTradeId(System.currentTimeMillis());
                newHeldTrade.setAssetSymbol(asset.getAssetSymbol());
                newHeldTrade.setQuantity(quantity);
                newHeldTrade.setBuyPriceUsd(price);
                newHeldTrade.setCreatedAt(OffsetDateTime.now());
                heldTradeRepository.save(newHeldTrade);

                logger.info("Created new held trade for user {} asset {}: quantity={}, price={}",
                           user.getEmail(), asset.getAssetSymbol(), quantity, price);
            }
        } else if (tradeSide == TradeSide.SELL) {
            if (target != null) {
                BigDecimal newQuantity = target.getQuantity().subtract(quantity);
                if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    heldTradeRepository.delete(target);
                    logger.info("Removed held trade for user {} asset {} (sold all)",
                               user.getEmail(), asset.getAssetSymbol());
                } else {
                    target.setQuantity(newQuantity);
                    heldTradeRepository.save(target);
                    logger.info("Updated held trade for user {} asset {}: new quantity={}",
                               user.getEmail(), asset.getAssetSymbol(), newQuantity);
                }
            } else {
                logger.warn("User {} tried to sell {} {} but has no held position", 
                           user.getEmail(), quantity, asset.getAssetSymbol());
            }
        }
    }
}