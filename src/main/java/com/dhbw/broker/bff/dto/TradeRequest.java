package com.dhbw.broker.bff.dto;

import java.math.BigDecimal;

public class TradeRequest {
    private String assetSymbol;
    private BigDecimal quantity;
    private String side; // "BUY" or "SELL"

    // Getters and Setters
    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }
}