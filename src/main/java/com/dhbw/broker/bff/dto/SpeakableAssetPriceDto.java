package com.dhbw.broker.bff.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class SpeakableAssetPriceDto {

    private String name;
    private String symbol;
    private BigDecimal priceUsd;
    private String updatedAt;

    public SpeakableAssetPriceDto() {
    }

    public SpeakableAssetPriceDto(String name, String symbol, BigDecimal priceUsd, String updatedAt) {
        this.name = name;
        this.symbol = symbol;
        this.priceUsd = priceUsd;
        this.updatedAt = updatedAt;
    }

}
