package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "asset_last_price")
public class AssetLastPrice {

    @Id
    @Column(name = "asset_symbol", length = 10, nullable = false)
    private String assetSymbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_symbol", referencedColumnName = "asset_symbol", insertable = false, updatable = false)
    private Asset asset;

    @Column(name = "price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "source_ts_utc", nullable = false)
    private OffsetDateTime sourceTsUtc;

    @Column(name = "is_carry", nullable = false)
    private boolean carry;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private OffsetDateTime updatedAt;
}