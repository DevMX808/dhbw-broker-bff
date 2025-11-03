package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
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

}
