package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.HeldTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeldTradeRepository extends JpaRepository<HeldTrade, Long> {
    List<HeldTrade> findByUserId(java.util.UUID userId);
  
    java.util.Optional<HeldTrade> findByUserIdAndAssetSymbol(java.util.UUID userId, String assetSymbol);
    
    List<HeldTrade> findAllByUserIdAndAssetSymbol(java.util.UUID userId, String assetSymbol);
    void deleteByTradeId(Long tradeId);
}
