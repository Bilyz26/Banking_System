package com.bankingsystem.customer.domain;

import com.bankingsystem.shared.domain.DomainException;

public final class InvalidCustomerProfileException extends DomainException {

    public InvalidCustomerProfileException(String message) {
        super(message);
    }
}

