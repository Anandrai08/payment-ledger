package com.ledger.domain;

import com.ledger.domain.enums.TransactionStatus;
import com.ledger.domain.enums.TransactionType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private Long sourceWalletId;

    private Long destinationWalletId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long fee;

    @Column(length = 500)
    private String description;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant completedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Transaction() {}

    public Transaction(String transactionRef, String idempotencyKey, TransactionType type,
                       Long sourceWalletId, Long destinationWalletId, long amount,
                       long fee, String description) {
        this.transactionRef = transactionRef;
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.status = TransactionStatus.INITIATED;
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.fee = fee;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getTransactionRef() { return transactionRef; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public Long getSourceWalletId() { return sourceWalletId; }
    public Long getDestinationWalletId() { return destinationWalletId; }
    public long getAmount() { return amount; }
    public long getFee() { return fee; }
    public String getDescription() { return description; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
