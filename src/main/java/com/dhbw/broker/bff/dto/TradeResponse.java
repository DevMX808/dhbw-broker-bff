package com.dhbw.broker.bff.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TradeResponse(
        UUID tradeId,
        OffsetDateTime executedAt,
        BigDecimal priceUsd,
        String assetSymbol,
        String side,
        BigDecimal quantity,
        BigDecimal total
) {
    public static TradeResponse from(UUID tradeId, OffsetDateTime executedAt, BigDecimal priceUsd,
                                     String assetSymbol, String side, BigDecimal quantity) {
        BigDecimal total = priceUsd.multiply(quantity);
        return new TradeResponse(tradeId, executedAt, priceUsd, assetSymbol, side, quantity, total);
    }
}