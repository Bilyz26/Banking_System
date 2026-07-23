package com.bankingsystem.transfer.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

import java.util.Objects;

public record TransferMoneyCommand(
        AccountId sourceAccountId,
        AccountId destinationAccountId,
        Money amount,
        String description,
        LedgerTransactionId idempotencyKey) {

    public TransferMoneyCommand {
        Objects.requireNonNull(sourceAccountId, "source account id must not be null");
        Objects.requireNonNull(destinationAccountId, "destination account id must not be null");
        Objects.requireNonNull(amount, "transfer amount must not be null");
    }

    public TransferMoneyCommand(
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            Money amount,
            String description) {
        this(sourceAccountId, destinationAccountId, amount, description, null);
    }
}
