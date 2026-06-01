package com.ledger.repository;

import com.ledger.domain.Wallet;
import com.ledger.domain.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByAccountId(Long accountId);
    Optional<Wallet> findByAccountIdAndCurrency(Long accountId, Currency currency);
}
