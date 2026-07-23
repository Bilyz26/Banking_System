package com.bankingsystem.customer.presentation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerProfileRequest(
        @NotBlank(message = "fullName is required")
        @Size(max = 200, message = "fullName must not exceed 200 characters")
        String fullName,

        @NotBlank(message = "emailAddress is required")
        @Email(message = "emailAddress must be valid")
        @Size(max = 320, message = "emailAddress must not exceed 320 characters")
        String emailAddress) {
}
