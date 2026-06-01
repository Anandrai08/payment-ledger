package com.ledger.domain;

import com.ledger.domain.enums.EntryType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionRef;

    @Column(nullable = false)
    private Long walletId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long runningBalance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public LedgerEntry() {}

    public LedgerEntry(String transactionRef, Long walletId, EntryType entryType,
                       long amount, long runningBalance) {
        this.transactionRef = transactionRef;
        this.walletId = walletId;
        this.entryType = entryType;
        this.amount = amount;
        this.runningBalance = runningBalance;
    }

    public Long getId() { return id; }
    public String getTransactionRef() { return transactionRef; }
    public Long getWalletId() { return walletId; }
    public EntryType getEntryType() { return entryType; }
    public long getAmount() { return amount; }
    public long getRunningBalance() { return runningBalance; }
    public Instant getCreatedAt() { return createdAt; }
}
