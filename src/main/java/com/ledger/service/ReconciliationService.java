package com.ledger.service;

import com.ledger.domain.LedgerEntry;
import com.ledger.domain.Transaction;
import com.ledger.domain.enums.EntryType;
import com.ledger.domain.enums.TransactionStatus;
import com.ledger.repository.LedgerEntryRepository;
import com.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public ReconciliationService(TransactionRepository transactionRepository,
                                 LedgerEntryRepository ledgerEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void reconcile() {
        log.info("Starting daily reconciliation");

        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(cutoff))
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .toList();

        int matched = 0;
        int breaks = 0;

        for (Transaction tx : transactions) {
            List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionRef(tx.getTransactionRef());
            long debitSum = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.DEBIT)
                    .mapToLong(LedgerEntry::getAmount)
                    .sum();
            long creditSum = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.CREDIT)
                    .mapToLong(LedgerEntry::getAmount)
                    .sum();

            if (debitSum == creditSum) {
                matched++;
            } else {
                breaks++;
                log.warn("Reconciliation break for transaction {}: debits={}, credits={}",
                        tx.getTransactionRef(), debitSum, creditSum);
            }
        }

        log.info("Reconciliation complete: {} matched, {} breaks", matched, breaks);
    }

    public ReconciliationSummary getSummary() {
        List<Transaction> all = transactionRepository.findAll();
        long total = all.size();
        long success = all.stream().filter(t -> t.getStatus() == TransactionStatus.SUCCESS).count();
        long failed = all.stream().filter(t -> t.getStatus() == TransactionStatus.FAILED).count();
        long refunded = all.stream().filter(t -> t.getStatus() == TransactionStatus.REFUNDED).count();

        return new ReconciliationSummary(total, success, failed, refunded);
    }

    public record ReconciliationSummary(long total, long success, long failed, long refunded) {}
}
