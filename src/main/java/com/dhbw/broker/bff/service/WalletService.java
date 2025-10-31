package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.domain.WalletTransaction;
import com.dhbw.broker.bff.domain.WalletTransactionType;
import com.dhbw.broker.bff.repository.WalletTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(WalletTransactionRepository walletTransactionRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public BigDecimal getCurrentBalance(UUID userId) {
        return walletTransactionRepository.getCurrentBalance(userId);
    }

    public List<WalletTransaction> getTransactionHistory(UUID userId) {
        return walletTransactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public WalletTransaction addInitialCredit(User user, BigDecimal amount, String note) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setType(WalletTransactionType.INITIAL_CREDIT);
        tx.setAmountUsd(amount);
        tx.setNote(note);
        return walletTransactionRepository.save(tx);
    }

    @Transactional
    public WalletTransaction deductForTrade(User user, BigDecimal amount, String note) {
        // Prüfe, ob genug Guthaben vorhanden ist
        BigDecimal currentBalance = getCurrentBalance(user.getUserId());
        if (currentBalance.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Insufficient balance. Current: " + currentBalance + ", Required: " + amount);
        }

        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setType(WalletTransactionType.WITHDRAWAL);
        tx.setAmountUsd(amount);
        tx.setNote(note);
        return walletTransactionRepository.save(tx);
    }

    @Transactional
    public WalletTransaction addFromSale(User user, BigDecimal amount, String note) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setType(WalletTransactionType.DEPOSIT);
        tx.setAmountUsd(amount);
        tx.setNote(note);
        return walletTransactionRepository.save(tx);
    }

    @Transactional
    public WalletTransaction addFunds(UUID userId, BigDecimal amount, String note) {
        // User-Objekt ist für die Transaktion erforderlich, aber wir haben nur die ID
        // Erstelle ein minimales User-Objekt (nicht ideal, aber funktional)
        User user = new User();
        user.setUserId(userId);
        
        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setType(WalletTransactionType.DEPOSIT);
        tx.setAmountUsd(amount);
        tx.setNote(note);
        return walletTransactionRepository.save(tx);
    }
}