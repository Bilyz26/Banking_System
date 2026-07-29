package com.bankingsystem.account.application;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.application.ApplicationException;

public final class ConcurrentAccountModificationException extends ApplicationException {

    public ConcurrentAccountModificationException(AccountId accountId) {
        super("Account '%s' was changed by another operation; retry with fresh data"
                .formatted(accountId.value()));
    }
}
