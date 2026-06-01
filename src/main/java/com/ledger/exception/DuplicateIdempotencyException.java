package com.ledger.exception;

public class DuplicateIdempotencyException extends RuntimeException {
    public DuplicateIdempotencyException(String idempotencyKey, String existingTransactionRef) {
        super("Duplicate idempotency key '" + idempotencyKey + "' already processed as " + existingTransactionRef);
    }
}
