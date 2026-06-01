package com.ledger.repository;

import com.ledger.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTransactionRefOrderByCreatedAtAsc(String transactionRef);
}
