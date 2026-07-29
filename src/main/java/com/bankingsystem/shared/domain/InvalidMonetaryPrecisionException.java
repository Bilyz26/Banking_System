package com.bankingsystem.shared.domain;

import java.math.BigDecimal;
import java.util.Currency;

public final class InvalidMonetaryPrecisionException extends DomainException {

    public InvalidMonetaryPrecisionException(
            BigDecimal amount,
            Currency currency,
            int allowedScale) {
        super("Amount %s has invalid precision for %s; at most %d fractional digits are allowed"
                .formatted(amount.toPlainString(), currency.getCurrencyCode(), allowedScale));
    }
}

