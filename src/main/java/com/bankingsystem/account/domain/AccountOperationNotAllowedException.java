package com.bankingsystem.account.domain;

public final class AccountOperationNotAllowedException extends AccountException {

    public AccountOperationNotAllowedException(AccountStatus status, String operation) {
        super("Cannot %s an account with status %s".formatted(operation, status));
    }
}

