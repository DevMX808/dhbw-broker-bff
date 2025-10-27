package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.WalletTransaction;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final IdentityService identityService;

    public WalletController(WalletService walletService, IdentityService identityService) {
        this.walletService = walletService;
        this.identityService = identityService;
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        var user = identityService.getCurrentUser();
        BigDecimal balance = walletService.getCurrentBalance(user.getUserId());
        
        return ResponseEntity.ok(Map.of(
            "balance", balance,
            "currency", "USD"
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions() {
        var user = identityService.getCurrentUser();
        List<WalletTransaction> transactions = walletService.getTransactionHistory(user.getUserId());
        
        List<Map<String, Object>> response = transactions.stream()
            .map(tx -> {
                Map<String, Object> txMap = new java.util.HashMap<>();
                txMap.put("txId", tx.getTxId().toString());
                txMap.put("type", tx.getType().name());
                txMap.put("amountUsd", tx.getAmountUsd());
                txMap.put("note", tx.getNote() != null ? tx.getNote() : "");
                txMap.put("createdAt", tx.getCreatedAt().toString());
                return txMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

}