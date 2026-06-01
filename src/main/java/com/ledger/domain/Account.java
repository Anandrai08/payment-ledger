package com.ledger.domain;

import com.ledger.domain.enums.AccountStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

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

    public Account() {}

    public Account(String accountNumber, String name) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.status = AccountStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(AccountStatus status) { this.status = status; }
    public void setName(String name) { this.name = name; }
}
