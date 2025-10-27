package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.HeldTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeldTradeRepository extends JpaRepository<HeldTrade, Long> {
    List<HeldTrade> findByUserId(java.util.UUID userId);
    void deleteByTradeId(Long tradeId);
}
