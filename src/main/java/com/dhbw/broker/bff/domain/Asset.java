package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
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
}