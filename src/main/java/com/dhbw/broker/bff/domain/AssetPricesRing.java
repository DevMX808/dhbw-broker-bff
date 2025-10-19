package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "asset_prices_ring",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_asset_prices_ring_symbol_ts", columnNames = {"asset_symbol", "source_ts_utc"})
        }
)
public class AssetPricesRing {

    @EmbeddedId
    private AssetPricesRingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_symbol", referencedColumnName = "asset_symbol", insertable = false, updatable = false)
    private Asset asset;

    @Column(name = "price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "source_ts_utc", nullable = false)
    private OffsetDateTime sourceTsUtc;

    @Column(name = "ingested_ts_utc", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime ingestedTsUtc;

    @Column(name = "is_carry", nullable = false)
    private boolean carry;

}