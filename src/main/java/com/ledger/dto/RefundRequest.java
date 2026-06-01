package com.ledger.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String originalTransactionRef,
        String description) {}
