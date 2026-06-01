package com.ledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotBlank String idempotencyKey,
        @NotNull Long sourceWalletId,
        @NotNull Long destinationWalletId,
        @Min(1) long amount,
        @Min(0) long fee,
        String description) {}
