package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.WalletTransaction;
import com.dhbw.broker.bff.domain.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    
    List<WalletTransaction> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);
    
    @Query("SELECT COALESCE(SUM(CASE WHEN wt.type IN ('DEPOSIT', 'INITIAL_CREDIT') THEN wt.amountUsd ELSE -wt.amountUsd END), 0) " +
           "FROM WalletTransaction wt WHERE wt.user.userId = :userId")
    BigDecimal getCurrentBalance(@Param("userId") UUID userId);
}
