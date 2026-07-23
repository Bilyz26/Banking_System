package com.bankingsystem.account.domain;

import com.bankingsystem.shared.domain.DomainException;

public abstract class AccountException extends DomainException {

    protected AccountException(String message) {
        super(message);
    }
}

