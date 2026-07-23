package com.bankingsystem.customer.application.port.in;

import com.bankingsystem.customer.domain.CustomerId;

/**
 * Stable application output returned after customer creation.
 */
public record CreateCustomerResult(
        CustomerId customerId,
        String fullName,
        String emailAddress) {
}

