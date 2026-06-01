package com.ledger.domain;

import com.ledger.domain.enums.Currency;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Wallet() {}

    public Wallet(Account account, Currency currency) {
        this.account = account;
        this.currency = currency;
        this.balance = 0;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public Currency getCurrency() { return currency; }
    public long getBalance() { return balance; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setBalance(long balance) { this.balance = balance; }
}
