package com.bankingsystem.customer.presentation;

import java.util.UUID;

public record CreateCustomerResponse(
        UUID customerId,
        String fullName,
        String emailAddress) {
}

