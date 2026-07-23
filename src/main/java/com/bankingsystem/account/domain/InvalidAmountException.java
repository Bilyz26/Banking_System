package com.bankingsystem.account.domain;

public final class InvalidAmountException extends AccountException {

    public InvalidAmountException() {
        super("Amount must be greater than zero");
    }
}

