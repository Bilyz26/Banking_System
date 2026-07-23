package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

public record DepositMoneyCommand(
        AccountId accountId,
        Money amount,
        String description,
        LedgerTransactionId idempotencyKey) {

    public DepositMoneyCommand(AccountId accountId, Money amount, String description) {
        this(accountId, amount, description, null);
    }
}
