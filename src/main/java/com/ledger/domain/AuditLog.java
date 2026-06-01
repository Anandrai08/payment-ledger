package com.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionRef;

    @Column(nullable = false)
    private String fromStatus;

    @Column(nullable = false)
    private String toStatus;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public AuditLog() {}

    public AuditLog(String transactionRef, String fromStatus, String toStatus, String notes) {
        this.transactionRef = transactionRef;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public String getTransactionRef() { return transactionRef; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
