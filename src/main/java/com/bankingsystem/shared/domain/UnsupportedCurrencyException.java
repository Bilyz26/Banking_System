package com.bankingsystem.shared.domain;

import java.util.Currency;

public final class UnsupportedCurrencyException extends DomainException {

    public UnsupportedCurrencyException(Currency currency) {
        super("Currency '%s' does not define a supported minor-unit precision"
                .formatted(currency.getCurrencyCode()));
    }
}

