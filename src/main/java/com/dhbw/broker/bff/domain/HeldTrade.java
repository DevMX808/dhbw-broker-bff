package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "held_trades")
public class HeldTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private java.util.UUID userId;

    @Column(name = "trade_id", nullable = false, unique = true)
    private Long tradeId;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "buy_price_usd", nullable = false)
    private BigDecimal buyPriceUsd;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public java.util.UUID getUserId() { return userId; }
    public void setUserId(java.util.UUID userId) { this.userId = userId; }
    public Long getTradeId() { return tradeId; }
    public void setTradeId(Long tradeId) { this.tradeId = tradeId; }
    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getBuyPriceUsd() { return buyPriceUsd; }
    public void setBuyPriceUsd(BigDecimal buyPriceUsd) { this.buyPriceUsd = buyPriceUsd; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
