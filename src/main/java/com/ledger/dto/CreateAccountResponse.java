package com.ledger.dto;

import com.ledger.domain.Account;
import com.ledger.domain.Wallet;

import java.util.List;

public record CreateAccountResponse(
        Long accountId,
        String accountNumber,
        String name,
        List<WalletInfo> wallets) {

    public record WalletInfo(Long walletId, String currency, long balance) {}

    public static CreateAccountResponse from(Account account, List<Wallet> wallets) {
        List<WalletInfo> walletInfos = wallets.stream()
                .map(w -> new WalletInfo(w.getId(), w.getCurrency().name(), w.getBalance()))
                .toList();
        return new CreateAccountResponse(account.getId(), account.getAccountNumber(),
                account.getName(), walletInfos);
    }
}
