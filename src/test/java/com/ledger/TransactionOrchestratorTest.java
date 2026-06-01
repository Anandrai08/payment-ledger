package com.ledger;

import com.ledger.domain.Account;
import com.ledger.domain.Transaction;
import com.ledger.domain.Wallet;
import com.ledger.domain.enums.AccountStatus;
import com.ledger.domain.enums.Currency;
import com.ledger.domain.enums.TransactionStatus;
import com.ledger.exception.AccountFrozenException;
import com.ledger.exception.DuplicateIdempotencyException;
import com.ledger.exception.InsufficientBalanceException;
import com.ledger.repository.AccountRepository;
import com.ledger.repository.TransactionRepository;
import com.ledger.repository.WalletRepository;
import com.ledger.service.AuditService;
import com.ledger.service.LedgerService;
import com.ledger.service.TransactionOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionOrchestratorTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private LedgerService ledgerService;
    @Mock private AuditService auditService;

    private TransactionOrchestrator orchestrator;
    private Account account;
    private Wallet sourceWallet;
    private Wallet destWallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        orchestrator = new TransactionOrchestrator(transactionRepository, walletRepository,
                accountRepository, ledgerService, auditService);

        account = new Account("ACC-001", "Test");
        sourceWallet = new Wallet(account, Currency.INR);
        sourceWallet.setBalance(1000);
        destWallet = new Wallet(account, Currency.INR);
        destWallet.setBalance(500);
        transaction = new Transaction("TXN-001", "idem-key", null, 1L, 2L, 200, 10, "test");
        transaction.setStatus(TransactionStatus.SUCCESS);
    }

    @Test
    void shouldRejectDuplicateIdempotencyKey() {
        when(transactionRepository.existsByIdempotencyKey("dup-key")).thenReturn(true);
        when(transactionRepository.findByIdempotencyKey("dup-key")).thenReturn(java.util.Optional.of(transaction));

        assertThrows(DuplicateIdempotencyException.class,
                () -> orchestrator.transfer("dup-key", 1L, 2L, 200, 10, "dup"));
    }

    @Test
    void shouldRejectTransferFromFrozenAccount() {
        account.setStatus(AccountStatus.FROZEN);
        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(walletRepository.findById(1L)).thenReturn(java.util.Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(java.util.Optional.of(destWallet));
        when(accountRepository.findById(account.getId())).thenReturn(java.util.Optional.of(account));

        assertThrows(AccountFrozenException.class,
                () -> orchestrator.transfer("key", 1L, 2L, 100, 0, "test"));
    }

    @Test
    void shouldCompleteSuccessfulTransfer() {
        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(walletRepository.findById(1L)).thenReturn(java.util.Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(java.util.Optional.of(destWallet));
        when(accountRepository.findById(account.getId())).thenReturn(java.util.Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(ledgerService).postEntry(any(Transaction.class));

        Transaction result = orchestrator.transfer("key-1", 1L, 2L, 200, 10, "test");

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(auditService, times(3)).log(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldMarkTransactionFailedOnException() {
        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(walletRepository.findById(1L)).thenReturn(java.util.Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(java.util.Optional.of(destWallet));
        when(accountRepository.findById(account.getId())).thenReturn(java.util.Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("DB connection error")).when(ledgerService).postEntry(any(Transaction.class));

        Transaction result = orchestrator.transfer("key-2", 1L, 2L, 200, 10, "test");

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertNotNull(result.getFailureReason());
    }
}
