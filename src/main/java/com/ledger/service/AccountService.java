package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.Wallet;
import com.ledger.domain.enums.AccountStatus;
import com.ledger.domain.enums.Currency;
import com.ledger.exception.AccountNotFoundException;
import com.ledger.repository.AccountRepository;
import com.ledger.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;

    public AccountService(AccountRepository accountRepository, WalletRepository walletRepository) {
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Account createAccount(String name, Currency currency) {
        String accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Account account = new Account(accountNumber, name);
        account = accountRepository.save(account);

        Wallet wallet = new Wallet(account, currency);
        walletRepository.save(wallet);

        return account;
    }

    @Transactional
    public void freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Transactional
    public void unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public List<Wallet> getWallets(Long accountId) {
        return walletRepository.findByAccountId(accountId);
    }

    public long getBalance(Long accountId, Currency currency) {
        Wallet wallet = walletRepository.findByAccountIdAndCurrency(accountId, currency)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return wallet.getBalance();
    }
}
