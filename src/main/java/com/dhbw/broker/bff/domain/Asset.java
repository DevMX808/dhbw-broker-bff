package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @Column(name = "asset_symbol", length = 10, nullable = false)
    private String assetSymbol;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "min_trade_increment", nullable = false, precision = 18, scale = 2)
    private BigDecimal minTradeIncrement;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Getters and Setters
    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMinTradeIncrement() {
        return minTradeIncrement;
    }

    public void setMinTradeIncrement(BigDecimal minTradeIncrement) {
        this.minTradeIncrement = minTradeIncrement;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}