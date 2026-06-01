package com.ledger.exception;

public class AccountFrozenException extends RuntimeException {
    public AccountFrozenException(Long accountId) {
        super("Account " + accountId + " is frozen. No transactions allowed.");
    }
}
