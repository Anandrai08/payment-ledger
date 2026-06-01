package com.ledger.dto;

import com.ledger.domain.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotBlank String name,
        @NotNull Currency currency) {}
