package com.bankingsystem.shared.domain;

import java.util.Currency;

public final class CurrencyMismatchException extends DomainException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Currency mismatch: expected %s but received %s"
                .formatted(expected.getCurrencyCode(), actual.getCurrencyCode()));
    }
}

