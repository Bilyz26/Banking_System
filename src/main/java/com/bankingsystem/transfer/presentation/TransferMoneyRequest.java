package com.bankingsystem.transfer.presentation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequest(
        @NotNull(message = "sourceAccountId is required")
        UUID sourceAccountId,

        @NotNull(message = "destinationAccountId is required")
        UUID destinationAccountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero")
        @Digits(integer = 17, fraction = 3, message = "amount has too many digits")
        BigDecimal amount,

        @NotBlank(message = "currencyCode is required")
        @Pattern(
                regexp = "[A-Z]{3}",
                message = "currencyCode must be a three-letter uppercase code")
        String currencyCode,

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description) {
}
