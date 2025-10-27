package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.service.HeldTradeService;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/held-trades")
public class HeldTradeController {
    private final HeldTradeService heldTradeService;
    private final IdentityService identityService;

    public HeldTradeController(HeldTradeService heldTradeService, IdentityService identityService) {
        this.heldTradeService = heldTradeService;
        this.identityService = identityService;
    }

    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> getHeldTrades() {
        User user = identityService.getCurrentUser();
        List<HeldTrade> heldTrades = heldTradeService.getHeldTradesForUser(user.getUserId());
        List<Map<String, Object>> response = heldTrades.stream().map(trade -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", trade.getId());
            map.put("tradeId", trade.getTradeId());
            map.put("assetSymbol", trade.getAssetSymbol());
            map.put("quantity", trade.getQuantity());
            map.put("buyPriceUsd", trade.getBuyPriceUsd());
            map.put("createdAt", trade.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
