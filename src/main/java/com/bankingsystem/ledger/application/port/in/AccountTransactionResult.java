package com.bankingsystem.ledger.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

import java.time.Instant;

public record AccountTransactionResult(
        LedgerEntryId ledgerEntryId,
        LedgerTransactionId transactionId,
        AccountId accountId,
        LedgerEntryType type,
        Money amount,
        Money balanceAfter,
        Instant occurredAt,
        String description) {
}
