package com.bankingsystem.ledger.domain;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferLedgerEntriesTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-07-23T12:00:00Z");

    @Test
    void createsCorrelatedTransferDebitAndCreditEntries() {
        LedgerTransactionId transactionId = transactionId();

        TransferLedgerEntries entries = TransferLedgerEntries.posted(
                entryId("b26b851f-107b-4bcb-befa-56351ac93e80"),
                entryId("57a1c8b0-33bf-4cb5-889e-e522e29a9498"),
                transactionId,
                accountId("5f9d34f1-2e35-43cd-ab1a-81ae6dc2d5e1"),
                accountId("17a5d107-421c-4734-9a9e-e560c92c06d5"),
                Money.of("40.00", "USD"),
                Money.of("60.00", "USD"),
                Money.of("90.00", "USD"),
                OCCURRED_AT,
                "Invoice payment");

        assertEquals(LedgerEntryType.TRANSFER_DEBIT, entries.debitEntry().type());
        assertEquals(LedgerEntryType.TRANSFER_CREDIT, entries.creditEntry().type());
        assertEquals(transactionId, entries.debitEntry().transactionId());
        assertEquals(transactionId, entries.creditEntry().transactionId());
        assertEquals(entries.debitEntry().amount(), entries.creditEntry().amount());
        assertEquals(OCCURRED_AT, entries.debitEntry().occurredAt());
        assertEquals(OCCURRED_AT, entries.creditEntry().occurredAt());
    }

    @Test
    void rejectsTransferEntriesForTheSameAccount() {
        AccountId accountId = accountId("5f9d34f1-2e35-43cd-ab1a-81ae6dc2d5e1");

        assertThrows(
                InvalidLedgerEntryException.class,
                () -> TransferLedgerEntries.posted(
                        entryId("b26b851f-107b-4bcb-befa-56351ac93e80"),
                        entryId("57a1c8b0-33bf-4cb5-889e-e522e29a9498"),
                        transactionId(),
                        accountId,
                        accountId,
                        Money.of("40.00", "USD"),
                        Money.of("60.00", "USD"),
                        Money.of("140.00", "USD"),
                        OCCURRED_AT,
                        "Invalid transfer"));
    }

    @Test
    void rejectsManuallyMismatchedTransferPairs() {
        LedgerTransactionId transactionId = transactionId();
        LedgerEntry debit = transferEntry(
                LedgerEntryType.TRANSFER_DEBIT,
                entryId("b26b851f-107b-4bcb-befa-56351ac93e80"),
                transactionId,
                accountId("5f9d34f1-2e35-43cd-ab1a-81ae6dc2d5e1"),
                Money.of("40.00", "USD"));
        LedgerEntry credit = transferEntry(
                LedgerEntryType.TRANSFER_CREDIT,
                entryId("57a1c8b0-33bf-4cb5-889e-e522e29a9498"),
                transactionId,
                accountId("17a5d107-421c-4734-9a9e-e560c92c06d5"),
                Money.of("39.00", "USD"));

        assertThrows(
                InvalidLedgerEntryException.class,
                () -> new TransferLedgerEntries(debit, credit));
    }

    private static LedgerEntry transferEntry(
            LedgerEntryType type,
            LedgerEntryId entryId,
            LedgerTransactionId transactionId,
            AccountId accountId,
            Money amount) {
        return new LedgerEntry(
                entryId,
                transactionId,
                accountId,
                type,
                amount,
                Money.of("60.00", "USD"),
                OCCURRED_AT,
                "Transfer");
    }

    private static LedgerEntryId entryId(String value) {
        return new LedgerEntryId(UUID.fromString(value));
    }

    private static LedgerTransactionId transactionId() {
        return new LedgerTransactionId(
                UUID.fromString("85c6c2be-f577-449c-a684-ab764d2be93e"));
    }

    private static AccountId accountId(String value) {
        return new AccountId(UUID.fromString(value));
    }
}

