package com.dhbw.broker.bff.repository;

import com.dhbw.broker.bff.domain.WalletAccount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, UUID> {
}