package com.bankingsystem.account.domain;

import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.Money;

import java.util.Currency;
import java.util.Objects;

/**
 * Aggregate root responsible for protecting account balance and lifecycle rules.
 */
public final class BankAccount {

    private final AccountId id;
    private final CustomerId ownerId;
    private Money balance;
    private AccountStatus status;

    private BankAccount(
            AccountId id,
            CustomerId ownerId,
            Money balance,
            AccountStatus status) {
        this.id = Objects.requireNonNull(id, "account id must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "owner id must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");

        if (balance.amount().signum() < 0) {
            throw new IllegalArgumentException("account balance must not be negative");
        }
        if (status == AccountStatus.CLOSED && !balance.isZero()) {
            throw new NonZeroBalanceException();
        }
    }

    public static BankAccount open(
            AccountId accountId,
            CustomerId ownerId,
            Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        return new BankAccount(
                accountId,
                ownerId,
                Money.zero(currency),
                AccountStatus.ACTIVE);
    }

    public static BankAccount restore(
            AccountId accountId,
            CustomerId ownerId,
            Money balance,
            AccountStatus status) {
        return new BankAccount(accountId, ownerId, balance, status);
    }

    public void deposit(Money amount) {
        validateDeposit(amount);
        balance = balance.add(amount);
    }

    public void withdraw(Money amount) {
        validateWithdrawal(amount);
        balance = balance.subtract(amount);
    }

    public void validateDeposit(Money amount) {
        requirePositive(amount);
        balance.add(amount);
        if (status == AccountStatus.CLOSED) {
            throw new AccountOperationNotAllowedException(status, "deposit into");
        }
    }

    public void validateWithdrawal(Money amount) {
        requirePositive(amount);
        balance.subtract(amount);
        if (status != AccountStatus.ACTIVE) {
            throw new AccountOperationNotAllowedException(status, "withdraw from");
        }
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(balance, amount);
        }
    }

    public void freeze() {
        requireStatus(AccountStatus.ACTIVE, "freeze");
        status = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        requireStatus(AccountStatus.FROZEN, "unfreeze");
        status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountOperationNotAllowedException(status, "close");
        }
        if (!balance.isZero()) {
            throw new NonZeroBalanceException();
        }
        status = AccountStatus.CLOSED;
    }

    private void requireStatus(AccountStatus requiredStatus, String operation) {
        if (status != requiredStatus) {
            throw new AccountOperationNotAllowedException(status, operation);
        }
    }

    private static void requirePositive(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (!amount.isPositive()) {
            throw new InvalidAmountException();
        }
    }

    public AccountId id() {
        return id;
    }

    public CustomerId ownerId() {
        return ownerId;
    }

    public Money balance() {
        return balance;
    }

    public AccountStatus status() {
        return status;
    }
}

