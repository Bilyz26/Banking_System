package com.bankingsystem.customer.application;

import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.application.ApplicationException;

public final class CustomerNotFoundException extends ApplicationException {

    public CustomerNotFoundException(CustomerId customerId) {
        super("Customer '%s' was not found".formatted(customerId.value()));
    }
}

