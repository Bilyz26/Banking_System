package com.bankingsystem.customer.application.port.in;

/**
 * Input required to register a customer.
 */
public record CreateCustomerCommand(String fullName, String emailAddress) {
}

