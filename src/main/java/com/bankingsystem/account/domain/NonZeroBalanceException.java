package com.bankingsystem.account.domain;

public final class NonZeroBalanceException extends AccountException {

    public NonZeroBalanceException() {
        super("An account can only be closed when its balance is zero");
    }
}

