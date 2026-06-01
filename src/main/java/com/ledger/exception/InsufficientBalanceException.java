package com.ledger.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long walletId, long balance, long required) {
        super("Insufficient balance in wallet " + walletId + ": available " + balance + ", required " + required);
    }
}
