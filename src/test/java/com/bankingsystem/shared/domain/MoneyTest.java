package com.bankingsystem.shared.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void addsAmountsWithTheSameCurrency() {
        Money result = Money.of("10.25", "USD").add(Money.of("2.75", "USD"));

        assertEquals(Money.of("13.00", "USD"), result);
    }

    @Test
    void rejectsDifferentCurrencies() {
        Money dollars = Money.of("10.00", "USD");
        Money euros = Money.of("10.00", "EUR");

        assertThrows(CurrencyMismatchException.class, () -> dollars.add(euros));
    }

    @Test
    void rejectsPrecisionBeyondTwoDecimalPlaces() {
        assertThrows(ArithmeticException.class, () -> Money.of("1.001", "USD"));
    }
}

