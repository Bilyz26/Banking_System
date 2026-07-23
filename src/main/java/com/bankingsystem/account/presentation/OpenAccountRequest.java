package com.bankingsystem.account.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record OpenAccountRequest(
        @NotNull(message = "ownerId is required")
        UUID ownerId,

        @NotBlank(message = "currencyCode is required")
        @Pattern(
                regexp = "[A-Z]{3}",
                message = "currencyCode must be a three-letter uppercase code")
        String currencyCode) {
}
