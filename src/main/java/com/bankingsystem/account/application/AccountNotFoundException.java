package com.bankingsystem.account.application;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.application.ApplicationException;

public final class AccountNotFoundException extends ApplicationException {

    public AccountNotFoundException(AccountId accountId) {
        super("Account '%s' was not found".formatted(accountId.value()));
    }
}

