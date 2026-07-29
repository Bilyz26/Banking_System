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
        assertThrows(
                InvalidMonetaryPrecisionException.class,
                () -> Money.of("1.001", "USD"));
    }

    @Test
    void supportsCurrencySpecificMinorUnits() {
        assertEquals("100", Money.of("100", "JPY").amount().toPlainString());
        assertEquals("1.234", Money.of("1.234", "BHD").amount().toPlainString());
    }

    @Test
    void rejectsFractionalAmountForZeroDecimalCurrency() {
        assertThrows(
                InvalidMonetaryPrecisionException.class,
                () -> Money.of("100.50", "JPY"));
    }

    @Test
    void rejectsAmountsOutsideSupportedStorageRange() {
        assertThrows(
                MonetaryAmountOutOfRangeException.class,
                () -> Money.of("100000000000000000.00", "USD"));
    }
}
