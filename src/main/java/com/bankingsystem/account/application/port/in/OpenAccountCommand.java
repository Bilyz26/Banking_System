package com.bankingsystem.account.application.port.in;

import com.bankingsystem.customer.domain.CustomerId;

/**
 * Input required to open a bank account.
 */
public record OpenAccountCommand(CustomerId ownerId, String currencyCode) {
}

