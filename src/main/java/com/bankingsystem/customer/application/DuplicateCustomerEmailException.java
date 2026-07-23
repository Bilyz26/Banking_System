package com.bankingsystem.customer.application;

import com.bankingsystem.shared.application.ApplicationException;

public final class DuplicateCustomerEmailException extends ApplicationException {

    public DuplicateCustomerEmailException(String emailAddress) {
        super("A customer with email address '%s' already exists".formatted(emailAddress));
    }
}
