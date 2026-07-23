package com.bankingsystem.transfer.domain;

import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.shared.domain.Money;

import java.util.Objects;

/**
 * Coordinates a transfer whose rules span two account aggregates.
 */
public final class TransferService {

    public void transfer(
            BankAccount sourceAccount,
            BankAccount destinationAccount,
            Money amount) {
        Objects.requireNonNull(sourceAccount, "source account must not be null");
        Objects.requireNonNull(destinationAccount, "destination account must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        if (sourceAccount.id().equals(destinationAccount.id())) {
            throw new SameAccountTransferException();
        }

        sourceAccount.validateWithdrawal(amount);
        destinationAccount.validateDeposit(amount);

        sourceAccount.withdraw(amount);
        destinationAccount.deposit(amount);
    }
}

