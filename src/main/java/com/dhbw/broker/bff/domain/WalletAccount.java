package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_accounts")
public class WalletAccount {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Setter
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public void setUserId(Object id) {
        this.userId = UUID.fromString(id.toString());
    }
}