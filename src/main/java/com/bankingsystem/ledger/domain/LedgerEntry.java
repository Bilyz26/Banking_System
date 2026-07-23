package com.bankingsystem.ledger.domain;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.CurrencyMismatchException;
import com.bankingsystem.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * An immutable, posted financial fact. Posted entries are never modified.
 */
public record LedgerEntry(
        LedgerEntryId id,
        LedgerTransactionId transactionId,
        AccountId accountId,
        LedgerEntryType type,
        Money amount,
        Money balanceAfter,
        Instant occurredAt,
        String description) {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    public LedgerEntry {
        Objects.requireNonNull(id, "ledger entry id must not be null");
        Objects.requireNonNull(transactionId, "ledger transaction id must not be null");
        Objects.requireNonNull(accountId, "account id must not be null");
        Objects.requireNonNull(type, "ledger entry type must not be null");
        Objects.requireNonNull(amount, "ledger amount must not be null");
        Objects.requireNonNull(balanceAfter, "resulting balance must not be null");
        Objects.requireNonNull(occurredAt, "ledger occurrence time must not be null");

        if (!amount.isPositive()) {
            throw new InvalidLedgerEntryException("ledger amount must be greater than zero");
        }
        if (!amount.currency().equals(balanceAfter.currency())) {
            throw new CurrencyMismatchException(amount.currency(), balanceAfter.currency());
        }
        if (balanceAfter.amount().signum() < 0) {
            throw new InvalidLedgerEntryException("resulting balance must not be negative");
        }

        description = normalizeDescription(description);
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }

        String normalizedDescription = description.trim();
        if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidLedgerEntryException(
                    "ledger description must not exceed %d characters"
                            .formatted(MAX_DESCRIPTION_LENGTH));
        }
        return normalizedDescription;
    }
}

