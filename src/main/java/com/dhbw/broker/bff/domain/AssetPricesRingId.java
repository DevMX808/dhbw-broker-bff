package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AssetPricesRingId implements Serializable {
    @Column(name = "asset_symbol", length = 10, nullable = false)
    private String assetSymbol;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    public AssetPricesRingId() {}
    public AssetPricesRingId(String assetSymbol, Integer slot) {
        this.assetSymbol = assetSymbol;
        this.slot = slot;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetPricesRingId that)) return false;
        return Objects.equals(assetSymbol, that.assetSymbol) && Objects.equals(slot, that.slot);
    }
    @Override public int hashCode() {
        return Objects.hash(assetSymbol, slot);
    }
}