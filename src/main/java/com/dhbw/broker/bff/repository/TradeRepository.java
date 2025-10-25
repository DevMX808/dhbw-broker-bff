package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.Trade;
import com.dhbw.broker.bff.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    
    @Query("SELECT t FROM Trade t JOIN FETCH t.asset WHERE t.user = :user ORDER BY t.executedAt DESC")
    List<Trade> findByUserOrderByExecutedAtDesc(@Param("user") User user);
    
    @Query("SELECT t FROM Trade t JOIN FETCH t.asset WHERE t.user = :user AND t.asset.assetSymbol = :assetSymbol ORDER BY t.executedAt DESC")
    List<Trade> findByUserAndAssetSymbolOrderByExecutedAtDesc(@Param("user") User user, @Param("assetSymbol") String assetSymbol);
}