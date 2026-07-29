package com.bankingsystem.ledger.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryLedgerRepositoryTest {

    @Test
    void appendsEntriesAndReturnsImmutableAccountHistory() {
        InMemoryLedgerRepository repository = new InMemoryLedgerRepository();
        LedgerEntry entry = ledgerEntry();

        repository.append(entry);

        assertEquals(
                java.util.List.of(entry),
                repository.findByAccountId(entry.accountId()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> repository.findByAccountId(entry.accountId()).add(entry));
    }

    @Test
    void rejectsDuplicateLedgerEntryIdentity() {
        InMemoryLedgerRepository repository = new InMemoryLedgerRepository();
        LedgerEntry entry = ledgerEntry();
        repository.append(entry);

        assertThrows(
                DuplicateLedgerEntryException.class,
                () -> repository.append(entry));
    }

    @Test
    void rejectsEntireBatchWhenOneEntryIdentityIsDuplicated() {
        InMemoryLedgerRepository repository = new InMemoryLedgerRepository();
        LedgerEntry existingEntry = ledgerEntry();
        LedgerEntry newEntry = ledgerEntry(
                UUID.fromString("e3530766-cc23-499c-870a-feaa7aa264b2"),
                UUID.fromString("552e7758-d8c9-4d15-a01c-2234456f03ee"));
        repository.append(existingEntry);

        assertThrows(
                DuplicateLedgerEntryException.class,
                () -> repository.appendAll(List.of(newEntry, existingEntry)));

        assertEquals(List.of(), repository.findByAccountId(newEntry.accountId()));
        assertEquals(List.of(existingEntry), repository.findByAccountId(existingEntry.accountId()));
    }

    private static LedgerEntry ledgerEntry() {
        return ledgerEntry(
                UUID.fromString("f4a93ed2-79be-49fb-9886-abc322595d1b"),
                UUID.fromString("4261d99d-9ba9-45e0-b55a-c7250f305e05"));
    }

    private static LedgerEntry ledgerEntry(UUID entryId, UUID accountId) {
        return new LedgerEntry(
                new LedgerEntryId(entryId),
                new LedgerTransactionId(
                        UUID.fromString("848c8477-755d-466e-a6cb-e950e12acbcb")),
                new AccountId(accountId),
                LedgerEntryType.DEPOSIT,
                Money.of("10.00", "USD"),
                Money.of("10.00", "USD"),
                Instant.parse("2026-07-23T12:00:00Z"),
                "Cash deposit");
    }
}
