package com.dhbw.broker.bff.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dhbw.broker.bff.domain.Trade;
import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.dto.TradeMessage;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.dto.TradeResponse;
import com.dhbw.broker.bff.repository.TradeRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final TradeRepository tradeRepository;
    private final IdentityService identityService;
    private final TradeQueueService tradeQueueService;
    private final boolean asyncTradeProcessing;

    public TradeServiceImpl(
            TradeRepository tradeRepository,
            IdentityService identityService,
            TradeQueueService tradeQueueService,
            @Value("${app.trade.async-processing:true}") boolean asyncTradeProcessing
    ) {
        this.tradeRepository = tradeRepository;
        this.identityService = identityService;
        this.tradeQueueService = tradeQueueService;
        this.asyncTradeProcessing = asyncTradeProcessing;
    }

    @Override
    public TradeResponse executeTrade(TradeRequest request) {
        if (request == null || request.getAssetSymbol() == null || request.getAssetSymbol().isBlank() ||
                request.getQuantity() == null || request.getSide() == null || request.getSide().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        User user = identityService.getCurrentUser();

        if (asyncTradeProcessing) {
            logger.info("Queueing trade for async processing: {} {} {} for user {}",
                    request.getSide(), request.getQuantity(), request.getAssetSymbol(), user.getEmail());

            TradeMessage message = TradeMessage.from(request, user.getUserId(), user.getEmail());

            try {
                tradeQueueService.sendTradeMessage(message);

                return new TradeResponse(
                        message.messageId(),
                        OffsetDateTime.now(),
                        null,
                        request.getAssetSymbol(),
                        request.getSide(),
                        request.getQuantity()
                );
            } catch (Exception e) {
                logger.error("Failed to queue trade message", e);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Trade konnte nicht in die Warteschlange gestellt werden");
            }
        } else {
            logger.warn("Synchronous trade processing is deprecated. Enable async processing in production.");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                    "Synchronous trade processing is disabled. Use async processing.");
        }
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