package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_lots",
        indexes = {
                @Index(name = "ix_lots_user_asset", columnList = "user_id, asset_symbol"),
                @Index(name = "ix_lots_user_asset_date", columnList = "user_id, asset_symbol, buy_date")
        }
)
public class PortfolioLot {

    @Id
    @Column(name = "lot_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID lotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_symbol", referencedColumnName = "asset_symbol", nullable = false)
    private Asset asset;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_trade_id", nullable = false, unique = true)
    private Trade buyTrade;

    @Column(name = "buy_date", nullable = false)
    private LocalDate buyDate;

    @Column(name = "buy_price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal buyPriceUsd;

    @Column(name = "initial_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal initialQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal remainingQuantity;

    @Column(name = "lot_cost_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal lotCostUsd;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
        // Getter-Methoden
        public UUID getLotId() { return lotId; }
        public User getUser() { return user; }
        public Asset getAsset() { return asset; }
        public Trade getBuyTrade() { return buyTrade; }
        public LocalDate getBuyDate() { return buyDate; }
        public BigDecimal getBuyPriceUsd() { return buyPriceUsd; }
        public BigDecimal getInitialQuantity() { return initialQuantity; }
        public BigDecimal getRemainingQuantity() { return remainingQuantity; }
        public BigDecimal getLotCostUsd() { return lotCostUsd; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
}