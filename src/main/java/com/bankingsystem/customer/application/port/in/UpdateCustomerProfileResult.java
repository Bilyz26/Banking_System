package com.bankingsystem.customer.application.port.in;

import com.bankingsystem.customer.domain.CustomerId;

public record UpdateCustomerProfileResult(
        CustomerId customerId,
        String fullName,
        String emailAddress) {
}

