package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.Trade;
import com.dhbw.broker.bff.dto.TradeRequest;
import com.dhbw.broker.bff.dto.TradeResponse;

import java.util.List;

public interface TradeService {
    TradeResponse executeTrade(TradeRequest request);
    List<Trade> getUserTrades();
    List<Trade> getUserTradesByAsset(String assetSymbol);
}