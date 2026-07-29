package com.bankingsystem.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An immutable monetary amount in one currency.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    private static final int MAX_INTEGER_DIGITS = 17;

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        int currencyScale = currency.getDefaultFractionDigits();
        if (currencyScale < 0) {
            throw new UnsupportedCurrencyException(currency);
        }

        try {
            amount = amount.setScale(currencyScale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidMonetaryPrecisionException(amount, currency, currencyScale);
        }
        int integerDigits = amount.precision() - amount.scale();
        if (integerDigits > MAX_INTEGER_DIGITS) {
            throw new MonetaryAmountOutOfRangeException(MAX_INTEGER_DIGITS);
        }
    }

    public static Money of(String amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }
}
