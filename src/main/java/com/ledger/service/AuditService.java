package com.ledger.service;

import com.ledger.domain.AuditLog;
import com.ledger.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String transactionRef, String fromStatus, String toStatus, String notes) {
        auditLogRepository.save(new AuditLog(transactionRef, fromStatus, toStatus, notes));
    }

    public List<AuditLog> getAuditTrail(String transactionRef) {
        return auditLogRepository.findByTransactionRefOrderByCreatedAtAsc(transactionRef);
    }
}
