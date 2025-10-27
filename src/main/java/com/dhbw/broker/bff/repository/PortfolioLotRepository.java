package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.PortfolioLot;
import com.dhbw.broker.bff.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PortfolioLotRepository extends JpaRepository<PortfolioLot, UUID> {
    @Query("SELECT l FROM PortfolioLot l WHERE l.user = :user AND l.remainingQuantity > 0")
    List<PortfolioLot> findHeldLotsByUser(@Param("user") User user);
}
