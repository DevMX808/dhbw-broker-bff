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

    // Alle aktuellen Trades eines Users abrufen
    public List<HeldTrade> getHeldTradesForUser(UUID userId) {
        return heldTradeRepository.findByUserId(userId);
    }

    // Neuen Trade speichern (Kauf)
    public HeldTrade addHeldTrade(HeldTrade trade) {
        return heldTradeRepository.save(trade);
    }

    // Trade löschen (Verkauf)
    public void removeHeldTrade(Long tradeId) {
        heldTradeRepository.deleteByTradeId(tradeId);
    }
}
