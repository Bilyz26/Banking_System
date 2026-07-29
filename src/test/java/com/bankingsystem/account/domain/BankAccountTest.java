package com.bankingsystem.account.domain;

import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.CurrencyMismatchException;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankAccountTest {

    @Test
    void depositsAndWithdrawsMoney() {
        BankAccount account = openUsdAccount();

        account.deposit(Money.of("100.00", "USD"));
        account.withdraw(Money.of("25.50", "USD"));

        assertEquals(Money.of("74.50", "USD"), account.balance());
    }

    @Test
    void rejectsOverdraft() {
        BankAccount account = openUsdAccount();
        account.deposit(Money.of("10.00", "USD"));

        assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(Money.of("10.01", "USD")));
        assertEquals(Money.of("10.00", "USD"), account.balance());
    }

    @Test
    void frozenAccountCanReceiveButCannotSendMoney() {
        BankAccount account = openUsdAccount();
        account.deposit(Money.of("10.00", "USD"));
        account.freeze();

        account.deposit(Money.of("5.00", "USD"));

        assertEquals(Money.of("15.00", "USD"), account.balance());
        assertThrows(
                AccountOperationNotAllowedException.class,
                () -> account.withdraw(Money.of("1.00", "USD")));
    }

    @Test
    void accountWithBalanceCannotBeClosed() {
        BankAccount account = openUsdAccount();
        account.deposit(Money.of("1.00", "USD"));

        assertThrows(NonZeroBalanceException.class, account::close);
    }

    @Test
    void zeroBalanceAccountCanBeClosed() {
        BankAccount account = openUsdAccount();

        account.close();

        assertEquals(AccountStatus.CLOSED, account.status());
        assertThrows(
                AccountOperationNotAllowedException.class,
                () -> account.deposit(Money.of("1.00", "USD")));
        assertThrows(
                AccountOperationNotAllowedException.class,
                () -> account.withdraw(Money.of("1.00", "USD")));
    }

    @Test
    void rejectsNonPositiveAmounts() {
        BankAccount account = openUsdAccount();

        assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(Money.of("0.00", "USD")));
        assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(Money.of("-1.00", "USD")));
    }

    @Test
    void currencyFailureDoesNotChangeBalance() {
        BankAccount account = openUsdAccount();
        account.deposit(Money.of("10.00", "USD"));

        assertThrows(
                CurrencyMismatchException.class,
                () -> account.deposit(Money.of("5.00", "EUR")));
        assertEquals(Money.of("10.00", "USD"), account.balance());
    }

    @Test
    void restoresOnlyValidPersistedState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BankAccount.restore(
                        AccountId.generate(),
                        CustomerId.generate(),
                        Money.of("-1.00", "USD"),
                        AccountStatus.ACTIVE));
        assertThrows(
                NonZeroBalanceException.class,
                () -> BankAccount.restore(
                        AccountId.generate(),
                        CustomerId.generate(),
                        Money.of("1.00", "USD"),
                        AccountStatus.CLOSED));
    }

    private static BankAccount openUsdAccount() {
        return BankAccount.open(
                AccountId.generate(),
                CustomerId.generate(),
                Currency.getInstance("USD"));
    }
}
