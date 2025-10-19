package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "ix_wallet_tx_user_time", columnList = "user_id, created_at")
})
public class WalletTransaction {

    @Id
    @Column(name = "tx_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID txId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private WalletTransactionType type;

    @Column(name = "amount_usd", nullable = false, precision = 20, scale = 2)
    private BigDecimal amountUsd;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "note", length = 500)
    private String note;

}