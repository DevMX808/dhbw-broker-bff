package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.PortfolioLot;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.service.PortfolioLotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioLotService portfolioLotService;
    private final IdentityService identityService;

    public PortfolioController(PortfolioLotService portfolioLotService, IdentityService identityService) {
        this.portfolioLotService = portfolioLotService;
        this.identityService = identityService;
    }

    @GetMapping("/held-assets")
    public ResponseEntity<List<Map<String, Object>>> getHeldAssets() {
        var user = identityService.getCurrentUser();
        List<PortfolioLot> lots = portfolioLotService.getHeldLotsForUser(user);
        List<Map<String, Object>> response = lots.stream().map(lot -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("lotId", lot.getLotId());
            map.put("assetSymbol", lot.getAsset().getAssetSymbol());
            map.put("buyDate", lot.getBuyDate());
            map.put("buyPriceUsd", lot.getBuyPriceUsd());
            map.put("initialQuantity", lot.getInitialQuantity());
            map.put("remainingQuantity", lot.getRemainingQuantity());
            map.put("lotCostUsd", lot.getLotCostUsd());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
