package com.ledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
        @NotBlank String idempotencyKey,
        @NotNull Long walletId,
        @Min(1) long amount,
        String description) {}
