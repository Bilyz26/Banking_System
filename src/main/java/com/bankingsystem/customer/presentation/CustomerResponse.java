package com.bankingsystem.customer.presentation;

import com.bankingsystem.customer.application.port.in.GetCustomerResult;

import java.util.UUID;

public record CustomerResponse(
        UUID customerId,
        String fullName,
        String emailAddress) {

    static CustomerResponse from(GetCustomerResult result) {
        return new CustomerResponse(
                result.customerId().value(),
                result.fullName(),
                result.emailAddress());
    }
}
