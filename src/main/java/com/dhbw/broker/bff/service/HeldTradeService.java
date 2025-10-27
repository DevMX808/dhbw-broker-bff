package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.repository.HeldTradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeldTradeService {
    private final HeldTradeRepository heldTradeRepository;

    public HeldTradeService(HeldTradeRepository heldTradeRepository) {
        this.heldTradeRepository = heldTradeRepository;
    }

    public List<HeldTrade> getHeldTradesForUser(java.util.UUID userId) {
        return heldTradeRepository.findByUserId(userId);
    }

    public void addHeldTrade(HeldTrade heldTrade) {
        heldTradeRepository.save(heldTrade);
    }

    public void removeHeldTradeByTradeId(Long tradeId) {
        heldTradeRepository.deleteByTradeId(tradeId);
    }
}
