package com.bankingsystem.account.domain;

import com.bankingsystem.shared.domain.Money;

public final class InsufficientFundsException extends AccountException {

    public InsufficientFundsException(Money balance, Money requested) {
        super("Insufficient funds: balance is %s %s but %s %s was requested"
                .formatted(
                        balance.amount(), balance.currency().getCurrencyCode(),
                        requested.amount(), requested.currency().getCurrencyCode()));
    }
}

