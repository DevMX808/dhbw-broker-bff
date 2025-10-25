package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, String> {
    
    @Query("SELECT a FROM Asset a WHERE a.active = true")
    List<Asset> findAllActive();
    
    @Query("SELECT a FROM Asset a WHERE a.assetSymbol = :assetSymbol AND a.active = true")
    Optional<Asset> findByAssetSymbolAndActive(String assetSymbol);
    
    boolean existsByAssetSymbolAndActive(String assetSymbol, boolean active);
}