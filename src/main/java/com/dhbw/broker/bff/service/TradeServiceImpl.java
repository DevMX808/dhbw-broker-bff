package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.*;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.dto.TradeResponse;
import com.dhbw.broker.bff.repository.AssetRepository;
import com.dhbw.broker.bff.repository.TradeRepository;
import com.dhbw.broker.bff.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final TradeRepository tradeRepository;
    private final AssetRepository assetRepository;
    private final IdentityService identityService;
    private final GraphqlPriceService priceService;
    private final UserRepository userRepository;

    public TradeServiceImpl(TradeRepository tradeRepository,
                           AssetRepository assetRepository,
                           IdentityService identityService,
                           GraphqlPriceService priceService,
                           UserRepository userRepository) {
        this.tradeRepository = tradeRepository;
        this.assetRepository = assetRepository;
        this.identityService = identityService;
        this.priceService = priceService;
        this.userRepository = userRepository;
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

        // Create and save trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setAsset(asset);
        trade.setSide(tradeSide);
        trade.setQuantity(request.getQuantity());
        trade.setPriceUsd(currentPrice);
        trade.setExecutedAt(OffsetDateTime.now());

        Trade savedTrade = tradeRepository.save(trade);
        
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


}