package com.bankingsystem.shared.domain;

public final class MonetaryAmountOutOfRangeException extends DomainException {

    public MonetaryAmountOutOfRangeException(int maximumIntegerDigits) {
        super("monetary amount must not exceed %d integer digits"
                .formatted(maximumIntegerDigits));
    }
}
