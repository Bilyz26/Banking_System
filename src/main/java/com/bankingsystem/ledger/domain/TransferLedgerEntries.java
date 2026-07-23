package com.bankingsystem.ledger.domain;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * The paired debit and credit facts produced by one completed transfer.
 */
public record TransferLedgerEntries(
        LedgerEntry debitEntry,
        LedgerEntry creditEntry) {

    public TransferLedgerEntries {
        Objects.requireNonNull(debitEntry, "transfer debit entry must not be null");
        Objects.requireNonNull(creditEntry, "transfer credit entry must not be null");

        requireType(debitEntry, LedgerEntryType.TRANSFER_DEBIT);
        requireType(creditEntry, LedgerEntryType.TRANSFER_CREDIT);

        if (debitEntry.accountId().equals(creditEntry.accountId())) {
            throw new InvalidLedgerEntryException(
                    "transfer ledger entries must belong to different accounts");
        }
        if (!debitEntry.transactionId().equals(creditEntry.transactionId())) {
            throw new InvalidLedgerEntryException(
                    "transfer ledger entries must share one transaction id");
        }
        if (!debitEntry.amount().equals(creditEntry.amount())) {
            throw new InvalidLedgerEntryException(
                    "transfer debit and credit amounts must match");
        }
        if (!debitEntry.occurredAt().equals(creditEntry.occurredAt())) {
            throw new InvalidLedgerEntryException(
                    "transfer ledger entries must share one occurrence time");
        }
    }

    public static TransferLedgerEntries posted(
            LedgerEntryId debitEntryId,
            LedgerEntryId creditEntryId,
            LedgerTransactionId transactionId,
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            Money amount,
            Money sourceBalanceAfter,
            Money destinationBalanceAfter,
            Instant occurredAt,
            String description) {
        LedgerEntry debitEntry = new LedgerEntry(
                debitEntryId,
                transactionId,
                sourceAccountId,
                LedgerEntryType.TRANSFER_DEBIT,
                amount,
                sourceBalanceAfter,
                occurredAt,
                description);
        LedgerEntry creditEntry = new LedgerEntry(
                creditEntryId,
                transactionId,
                destinationAccountId,
                LedgerEntryType.TRANSFER_CREDIT,
                amount,
                destinationBalanceAfter,
                occurredAt,
                description);

        return new TransferLedgerEntries(debitEntry, creditEntry);
    }

    private static void requireType(LedgerEntry entry, LedgerEntryType expectedType) {
        if (entry.type() != expectedType) {
            throw new InvalidLedgerEntryException(
                    "expected ledger entry type %s but received %s"
                            .formatted(expectedType, entry.type()));
        }
    }
}

