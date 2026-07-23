package com.bankingsystem.transfer.domain;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.domain.InsufficientFundsException;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.CurrencyMismatchException;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {

    private final TransferService transferService = new TransferService();

    @Test
    void transfersMoneyBetweenAccounts() {
        BankAccount source = openAccount("USD");
        BankAccount destination = openAccount("USD");
        source.deposit(Money.of("100.00", "USD"));

        transferService.transfer(source, destination, Money.of("40.00", "USD"));

        assertEquals(Money.of("60.00", "USD"), source.balance());
        assertEquals(Money.of("40.00", "USD"), destination.balance());
    }

    @Test
    void rejectsTransferToTheSameAccount() {
        BankAccount account = openAccount("USD");

        assertThrows(
                SameAccountTransferException.class,
                () -> transferService.transfer(account, account, Money.of("1.00", "USD")));
    }

    @Test
    void failedTransferDoesNotChangeEitherBalance() {
        BankAccount source = openAccount("USD");
        BankAccount destination = openAccount("USD");
        source.deposit(Money.of("10.00", "USD"));

        assertThrows(
                InsufficientFundsException.class,
                () -> transferService.transfer(
                        source,
                        destination,
                        Money.of("11.00", "USD")));

        assertEquals(Money.of("10.00", "USD"), source.balance());
        assertEquals(Money.of("0.00", "USD"), destination.balance());
    }

    @Test
    void currencyFailureDoesNotDebitSourceAccount() {
        BankAccount source = openAccount("USD");
        BankAccount destination = openAccount("EUR");
        source.deposit(Money.of("10.00", "USD"));

        assertThrows(
                CurrencyMismatchException.class,
                () -> transferService.transfer(
                        source,
                        destination,
                        Money.of("5.00", "USD")));

        assertEquals(Money.of("10.00", "USD"), source.balance());
        assertEquals(Money.of("0.00", "EUR"), destination.balance());
    }

    private static BankAccount openAccount(String currencyCode) {
        return BankAccount.open(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.getInstance(currencyCode));
    }
}
