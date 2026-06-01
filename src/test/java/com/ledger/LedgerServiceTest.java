package com.ledger;

import com.ledger.domain.Account;
import com.ledger.domain.LedgerEntry;
import com.ledger.domain.Transaction;
import com.ledger.domain.Wallet;
import com.ledger.domain.enums.Currency;
import com.ledger.domain.enums.TransactionType;
import com.ledger.exception.InsufficientBalanceException;
import com.ledger.repository.LedgerEntryRepository;
import com.ledger.repository.WalletRepository;
import com.ledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;

    private LedgerService ledgerService;
    private Account account;
    private Wallet sourceWallet;
    private Wallet destWallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(walletRepository, ledgerEntryRepository);

        account = new Account("ACC-001", "Test");
        sourceWallet = new Wallet(account, Currency.INR);
        sourceWallet.setBalance(1000);

        destWallet = new Wallet(account, Currency.INR);
        destWallet.setBalance(500);

        transaction = new Transaction("TXN-001", "idem-1", TransactionType.TRANSFER,
                1L, 2L, 200, 10, "test transfer");
    }

    @Test
    void shouldPostEntrySuccessfully() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(destWallet));
        when(walletRepository.save(any())).thenReturn(sourceWallet, destWallet);

        ledgerService.postEntry(transaction);

        assertEquals(790, sourceWallet.getBalance());
        assertEquals(700, destWallet.getBalance());
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        sourceWallet.setBalance(100);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));

        assertThrows(InsufficientBalanceException.class, () -> ledgerService.postEntry(transaction));
        verify(ledgerEntryRepository, never()).save(any());
    }

    @Test
    void shouldReverseEntryCorrectly() {
        sourceWallet.setBalance(790);
        destWallet.setBalance(700);

        Transaction refund = new Transaction("REF-001", "idem-2", TransactionType.REFUND,
                1L, 2L, 200, 0, "refund");

        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(destWallet));
        when(walletRepository.save(any())).thenReturn(sourceWallet, destWallet);

        ledgerService.reverseEntry(transaction, refund);

        assertEquals(1000, sourceWallet.getBalance());
        assertEquals(500, destWallet.getBalance());
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
    }
}
