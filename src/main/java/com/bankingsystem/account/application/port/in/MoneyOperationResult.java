package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

public record MoneyOperationResult(
        AccountId accountId,
        LedgerEntryId ledgerEntryId,
        LedgerTransactionId transactionId,
        Money balance) {
}
