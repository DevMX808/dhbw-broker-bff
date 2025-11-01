package com.dhbw.broker.bff.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TradeMessage(
        UUID userId,
        String userEmail,
        String assetSymbol,
        BigDecimal quantity,
        String side,
        UUID messageId
) {
    public static TradeMessage from(TradeRequest request, UUID userId, String userEmail) {
        return new TradeMessage(
                userId,
                userEmail,
                request.getAssetSymbol(),
                request.getQuantity(),
                request.getSide(),
                UUID.randomUUID()
        );
    }
}