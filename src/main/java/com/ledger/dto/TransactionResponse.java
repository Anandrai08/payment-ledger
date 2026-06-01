package com.ledger.dto;

import com.ledger.domain.Transaction;

import java.time.Instant;

public record TransactionResponse(
        String transactionRef,
        String idempotencyKey,
        String type,
        String status,
        Long sourceWalletId,
        Long destinationWalletId,
        long amount,
        long fee,
        String description,
        String failureReason,
        Instant createdAt,
        Instant completedAt) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getTransactionRef(), t.getIdempotencyKey(),
                t.getType().name(), t.getStatus().name(),
                t.getSourceWalletId(), t.getDestinationWalletId(),
                t.getAmount(), t.getFee(), t.getDescription(),
                t.getFailureReason(), t.getCreatedAt(), t.getCompletedAt());
    }
}
