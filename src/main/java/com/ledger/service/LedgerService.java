package com.ledger.service;

import com.ledger.domain.LedgerEntry;
import com.ledger.domain.Transaction;
import com.ledger.domain.Wallet;
import com.ledger.domain.enums.EntryType;
import com.ledger.domain.enums.TransactionStatus;
import com.ledger.exception.InsufficientBalanceException;
import com.ledger.exception.WalletNotFoundException;
import com.ledger.repository.LedgerEntryRepository;
import com.ledger.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void postEntry(Transaction transaction) {
        Wallet source = walletRepository.findById(transaction.getSourceWalletId())
                .orElseThrow(() -> new WalletNotFoundException(transaction.getSourceWalletId()));

        long totalDebit = transaction.getAmount() + transaction.getFee();
        if (source.getBalance() < totalDebit) {
            throw new InsufficientBalanceException(source.getId(), source.getBalance(), totalDebit);
        }

        long newSourceBalance = source.getBalance() - totalDebit;
        source.setBalance(newSourceBalance);
        walletRepository.save(source);

        ledgerEntryRepository.save(new LedgerEntry(
                transaction.getTransactionRef(), source.getId(),
                EntryType.DEBIT, totalDebit, newSourceBalance));

        if (transaction.getDestinationWalletId() != null) {
            Wallet dest = walletRepository.findById(transaction.getDestinationWalletId())
                    .orElseThrow(() -> new WalletNotFoundException(transaction.getDestinationWalletId()));

            long newDestBalance = dest.getBalance() + transaction.getAmount();
            dest.setBalance(newDestBalance);
            walletRepository.save(dest);

            ledgerEntryRepository.save(new LedgerEntry(
                    transaction.getTransactionRef(), dest.getId(),
                    EntryType.CREDIT, transaction.getAmount(), newDestBalance));
        }

        log.info("Ledger posted for transaction {}: source {} debited {}, destination {} credited {}",
                transaction.getTransactionRef(), source.getId(), totalDebit,
                transaction.getDestinationWalletId(), transaction.getAmount());
    }

    @Transactional
    public void reverseEntry(Transaction originalTransaction, Transaction refundTransaction) {
        Wallet source = walletRepository.findById(originalTransaction.getSourceWalletId())
                .orElseThrow(() -> new WalletNotFoundException(originalTransaction.getSourceWalletId()));

        long creditAmount = originalTransaction.getAmount() + originalTransaction.getFee();
        long newSourceBalance = source.getBalance() + creditAmount;
        source.setBalance(newSourceBalance);
        walletRepository.save(source);

        ledgerEntryRepository.save(new LedgerEntry(
                refundTransaction.getTransactionRef(), source.getId(),
                EntryType.CREDIT, creditAmount, newSourceBalance));

        if (originalTransaction.getDestinationWalletId() != null) {
            Wallet dest = walletRepository.findById(originalTransaction.getDestinationWalletId())
                    .orElseThrow(() -> new WalletNotFoundException(originalTransaction.getDestinationWalletId()));

            long newDestBalance = dest.getBalance() - originalTransaction.getAmount();
            dest.setBalance(newDestBalance);
            walletRepository.save(dest);

            ledgerEntryRepository.save(new LedgerEntry(
                    refundTransaction.getTransactionRef(), dest.getId(),
                    EntryType.DEBIT, originalTransaction.getAmount(), newDestBalance));
        }

        log.info("Ledger reversed for transaction {} via refund {}",
                originalTransaction.getTransactionRef(), refundTransaction.getTransactionRef());
    }

    public List<LedgerEntry> getStatement(Long walletId) {
        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }
}
