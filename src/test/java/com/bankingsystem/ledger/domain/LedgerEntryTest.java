package com.bankingsystem.ledger.domain;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.CurrencyMismatchException;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LedgerEntryTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-07-23T12:00:00Z");

    @Test
    void createsAnImmutablePostedDepositFact() {
        LedgerEntry entry = entry(
                LedgerEntryType.DEPOSIT,
                Money.of("25.00", "USD"),
                Money.of("125.00", "USD"),
                "  Cash deposit  ");

        assertEquals(LedgerDirection.CREDIT, entry.type().direction());
        assertEquals("Cash deposit", entry.description());
        assertEquals(OCCURRED_AT, entry.occurredAt());
    }

    @Test
    void classifiesDebitAndCreditEntryTypes() {
        assertEquals(LedgerDirection.CREDIT, LedgerEntryType.DEPOSIT.direction());
        assertEquals(LedgerDirection.CREDIT, LedgerEntryType.TRANSFER_CREDIT.direction());
        assertEquals(LedgerDirection.DEBIT, LedgerEntryType.WITHDRAWAL.direction());
        assertEquals(LedgerDirection.DEBIT, LedgerEntryType.TRANSFER_DEBIT.direction());
    }

    @Test
    void rejectsNonPositiveLedgerAmount() {
        assertThrows(
                InvalidLedgerEntryException.class,
                () -> entry(
                        LedgerEntryType.DEPOSIT,
                        Money.of("0.00", "USD"),
                        Money.of("100.00", "USD"),
                        "Invalid deposit"));
    }

    @Test
    void rejectsNegativeResultingBalance() {
        assertThrows(
                InvalidLedgerEntryException.class,
                () -> entry(
                        LedgerEntryType.WITHDRAWAL,
                        Money.of("10.00", "USD"),
                        Money.of("-1.00", "USD"),
                        "Invalid withdrawal"));
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertThrows(
                CurrencyMismatchException.class,
                () -> entry(
                        LedgerEntryType.DEPOSIT,
                        Money.of("10.00", "USD"),
                        Money.of("10.00", "EUR"),
                        "Invalid currency"));
    }

    @Test
    void rejectsOversizedDescription() {
        assertThrows(
                InvalidLedgerEntryException.class,
                () -> entry(
                        LedgerEntryType.DEPOSIT,
                        Money.of("10.00", "USD"),
                        Money.of("10.00", "USD"),
                        "D".repeat(501)));
    }

    private static LedgerEntry entry(
            LedgerEntryType type,
            Money amount,
            Money balanceAfter,
            String description) {
        return new LedgerEntry(
                new LedgerEntryId(UUID.fromString("ffb61eb1-26aa-4731-86d5-2df2937f92eb")),
                new LedgerTransactionId(
                        UUID.fromString("85c6c2be-f577-449c-a684-ab764d2be93e")),
                new AccountId(UUID.fromString("c0b81918-c50f-476f-8efa-36ba9ef054e7")),
                type,
                amount,
                balanceAfter,
                OCCURRED_AT,
                description);
    }
}

