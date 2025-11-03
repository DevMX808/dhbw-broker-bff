package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.HeldTrade;
import com.dhbw.broker.bff.service.HeldTradeService;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<HeldTrade>> getHeldTrades() {
        User user = identityService.getCurrentUser();
        return ResponseEntity.ok(heldTradeService.getHeldTradesForUser(user.getUserId()));
    }

    
    @PostMapping("/buy")
    public ResponseEntity<HeldTrade> buyTrade(@RequestBody HeldTrade trade) {
        User user = identityService.getCurrentUser();
        trade.setUserId(user.getUserId());
        trade.setCreatedAt(java.time.OffsetDateTime.now());
        return ResponseEntity.ok(heldTradeService.addHeldTrade(trade));
    }

    
    @DeleteMapping("/sell/{tradeId}")
    public ResponseEntity<Void> sellTrade(@PathVariable Long tradeId) {
        heldTradeService.removeHeldTrade(tradeId);
        return ResponseEntity.noContent().build();
    }
}
