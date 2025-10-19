package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lot_consumption",
        uniqueConstraints = @UniqueConstraint(name = "ux_sell_lot", columnNames = { "sell_trade_id", "lot_id" }),
        indexes = @Index(name = "ix_lot_consumption_lot", columnList = "lot_id")
)
public class LotConsumption {

    @Id
    @Column(name = "consumption_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID consumptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sell_trade_id", nullable = false)
    private Trade sellTrade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private PortfolioLot lot;

    @Column(name = "consumed_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal consumedQuantity;

    @Column(name = "cost_per_unit_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal costPerUnitUsd;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}