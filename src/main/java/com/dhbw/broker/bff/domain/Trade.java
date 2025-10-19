package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades",
        indexes = {
                @Index(name = "ix_trades_user_time", columnList = "user_id, executed_at"),
                @Index(name = "ix_trades_user_asset", columnList = "user_id, asset_symbol")
        }
)
public class Trade {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID tradeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_symbol", referencedColumnName = "asset_symbol", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 4)
    private TradeSide side;

    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}