package com.bankingsystem.account.application;

import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.application.ApplicationException;

public final class AccountOwnerNotFoundException extends ApplicationException {

    public AccountOwnerNotFoundException(CustomerId customerId) {
        super("Account owner '%s' was not found".formatted(customerId.value()));
    }
}

