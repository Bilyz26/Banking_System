package com.bankingsystem.ledger.presentation;

import com.bankingsystem.ledger.application.port.in.AccountTransactionResult;
import com.bankingsystem.ledger.domain.LedgerEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountTransactionResponse(
        UUID ledgerEntryId,
        UUID transactionId,
        LedgerEntryType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String currencyCode,
        Instant occurredAt,
        String description) {

    static AccountTransactionResponse from(AccountTransactionResult result) {
        return new AccountTransactionResponse(
                result.ledgerEntryId().value(),
                result.transactionId().value(),
                result.type(),
                result.amount().amount(),
                result.balanceAfter().amount(),
                result.amount().currency().getCurrencyCode(),
                result.occurredAt(),
                result.description());
    }
}
