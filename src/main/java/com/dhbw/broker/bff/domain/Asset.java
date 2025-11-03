package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "assets")
public class Asset {

    @Setter
    @Id
    @Column(name = "asset_symbol", length = 10, nullable = false)
    private String assetSymbol;

    @Setter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "min_trade_increment", nullable = false, precision = 18, scale = 2)
    private BigDecimal minTradeIncrement;

    @Setter
    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public void setMinTradeIncrement(BigDecimal minTradeIncrement) {
        this.minTradeIncrement = minTradeIncrement;
    }

}