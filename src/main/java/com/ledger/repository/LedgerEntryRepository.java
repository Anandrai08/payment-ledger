package com.ledger.repository;

import com.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByTransactionRef(String transactionRef);
    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
