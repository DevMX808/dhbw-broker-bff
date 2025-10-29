package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.HeldTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeldTradeRepository extends JpaRepository<HeldTrade, Long> {
    List<HeldTrade> findByUserId(java.util.UUID userId);
    // Find a single held position for a user and asset (preferred)
    java.util.Optional<HeldTrade> findByUserIdAndAssetSymbol(java.util.UUID userId, String assetSymbol);
    // Find all held positions for a user and asset (in case duplicates exist) for consolidation
    List<HeldTrade> findAllByUserIdAndAssetSymbol(java.util.UUID userId, String assetSymbol);
    void deleteByTradeId(Long tradeId);
}
