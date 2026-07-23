package com.bankingsystem.customer.application.port.in;

import com.bankingsystem.customer.domain.CustomerId;

public record UpdateCustomerProfileCommand(
        CustomerId customerId,
        String fullName,
        String emailAddress) {
}

