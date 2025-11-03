package com.dhbw.broker.bff.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.repository.HeldTradeRepository;

@Service
public class HeldTradeService {
    private final HeldTradeRepository heldTradeRepository;

    public HeldTradeService(HeldTradeRepository heldTradeRepository) {
        this.heldTradeRepository = heldTradeRepository;
    }

   
    public List<HeldTrade> getHeldTradesForUser(UUID userId) {
        return heldTradeRepository.findByUserId(userId);
    }


    public HeldTrade addHeldTrade(HeldTrade trade) {
        return heldTradeRepository.save(trade);
    }

  
    public void removeHeldTrade(Long tradeId) {
        heldTradeRepository.deleteByTradeId(tradeId);
    }
}
