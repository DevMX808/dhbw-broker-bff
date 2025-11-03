package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.WalletTransaction;
import com.dhbw.broker.bff.service.IdentityService;
import com.dhbw.broker.bff.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/add-funds")
    public ResponseEntity<Map<String, Object>> addFunds(@RequestBody Map<String, Object> request) {
        var user = identityService.getCurrentUser();
        
        
        Object amountObj = request.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountObj.toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("10000")) > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be between 0.01 and 10000"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount format"));
        }
        
   
        walletService.addFunds(user.getUserId(), amount, "Manual funds addition");
        
        
        BigDecimal newBalance = walletService.getCurrentBalance(user.getUserId());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "newBalance", newBalance,
            "addedAmount", amount,
            "message", "Funds added successfully"
        ));
    }

}