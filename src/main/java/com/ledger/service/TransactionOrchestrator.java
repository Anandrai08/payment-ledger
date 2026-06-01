package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.Transaction;
import com.ledger.domain.Wallet;
import com.ledger.domain.enums.AccountStatus;
import com.ledger.domain.enums.TransactionStatus;
import com.ledger.domain.enums.TransactionType;
import com.ledger.exception.AccountFrozenException;
import com.ledger.exception.AccountNotFoundException;
import com.ledger.exception.DuplicateIdempotencyException;
import com.ledger.exception.InsufficientBalanceException;
import com.ledger.exception.WalletNotFoundException;
import com.ledger.repository.AccountRepository;
import com.ledger.repository.TransactionRepository;
import com.ledger.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TransactionOrchestrator.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;

    public TransactionOrchestrator(TransactionRepository transactionRepository,
                                   WalletRepository walletRepository,
                                   AccountRepository accountRepository,
                                   LedgerService ledgerService,
                                   AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
    }

    @Transactional
    public Transaction transfer(String idempotencyKey, Long sourceWalletId,
                                Long destinationWalletId, long amount, long fee,
                                String description) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            Transaction existing = transactionRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
            throw new DuplicateIdempotencyException(idempotencyKey, existing.getTransactionRef());
        }

        Wallet source = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException(sourceWalletId));
        Wallet dest = walletRepository.findById(destinationWalletId)
                .orElseThrow(() -> new WalletNotFoundException(destinationWalletId));

        validateAccountStatus(source.getAccount().getId());
        validateAccountStatus(dest.getAccount().getId());

        String txRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Transaction transaction = new Transaction(txRef, idempotencyKey, TransactionType.TRANSFER,
                sourceWalletId, destinationWalletId, amount, fee, description);
        transaction = transactionRepository.save(transaction);
        auditService.log(transaction.getTransactionRef(), "INITIATED", "PENDING", "Transfer initiated");

        try {
            transaction.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(transaction);

            ledgerService.postEntry(transaction);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCompletedAt(Instant.now());
            transactionRepository.save(transaction);
            auditService.log(transaction.getTransactionRef(), "PENDING", "SUCCESS", "Transfer completed");
            log.info("Transfer {} completed: {} -> {} amount {}", txRef, sourceWalletId, destinationWalletId, amount);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(Instant.now());
            transactionRepository.save(transaction);
            auditService.log(transaction.getTransactionRef(), "PENDING", "FAILED", e.getMessage());
            log.error("Transfer {} failed: {}", txRef, e.getMessage());
        }

        return transaction;
    }

    @Transactional
    public Transaction deposit(String idempotencyKey, Long walletId, long amount, String description) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            Transaction existing = transactionRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
            throw new DuplicateIdempotencyException(idempotencyKey, existing.getTransactionRef());
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        validateAccountStatus(wallet.getAccount().getId());

        String txRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Transaction transaction = new Transaction(txRef, idempotencyKey, TransactionType.DEPOSIT,
                walletId, null, amount, 0, description);
        transaction = transactionRepository.save(transaction);
        auditService.log(transaction.getTransactionRef(), "INITIATED", "PENDING", "Deposit initiated");

        try {
            transaction.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(transaction);

            long newBalance = wallet.getBalance() + amount;
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCompletedAt(Instant.now());
            transactionRepository.save(transaction);
            auditService.log(transaction.getTransactionRef(), "PENDING", "SUCCESS", "Deposit completed");
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(Instant.now());
            transactionRepository.save(transaction);
            auditService.log(transaction.getTransactionRef(), "PENDING", "FAILED", e.getMessage());
        }

        return transaction;
    }

    @Transactional
    public Transaction refund(String idempotencyKey, String originalTransactionRef, String description) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            Transaction existing = transactionRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
            throw new DuplicateIdempotencyException(idempotencyKey, existing.getTransactionRef());
        }

        Transaction original = transactionRepository.findByTransactionRef(originalTransactionRef)
                .orElseThrow(() -> new IllegalArgumentException("Original transaction not found: " + originalTransactionRef));

        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund a successful transaction");
        }

        String txRef = "REF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Transaction refund = new Transaction(txRef, idempotencyKey, TransactionType.REFUND,
                original.getSourceWalletId(), original.getDestinationWalletId(),
                original.getAmount(), 0,
                description != null ? description : "Refund for " + originalTransactionRef);
        refund = transactionRepository.save(refund);
        auditService.log(refund.getTransactionRef(), "INITIATED", "PENDING", "Refund initiated");

        try {
            refund.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(refund);

            ledgerService.reverseEntry(original, refund);

            refund.setStatus(TransactionStatus.SUCCESS);
            refund.setCompletedAt(Instant.now());
            transactionRepository.save(refund);
            auditService.log(refund.getTransactionRef(), "PENDING", "SUCCESS", "Refund completed");

            original.setStatus(TransactionStatus.REFUNDED);
            transactionRepository.save(original);
        } catch (Exception e) {
            refund.setStatus(TransactionStatus.FAILED);
            refund.setFailureReason(e.getMessage());
            refund.setCompletedAt(Instant.now());
            transactionRepository.save(refund);
            auditService.log(refund.getTransactionRef(), "PENDING", "FAILED", e.getMessage());
        }

        return refund;
    }

    public Transaction getByTransactionRef(String transactionRef) {
        return transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionRef));
    }

    public Transaction getByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));
    }

    private void validateAccountStatus(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountId);
        }
    }
}
