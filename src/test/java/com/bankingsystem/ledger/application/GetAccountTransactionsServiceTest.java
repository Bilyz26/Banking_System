package com.bankingsystem.ledger.application;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.ledger.application.port.in.AccountTransactionPage;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsQuery;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.infrastructure.InMemoryLedgerRepository;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetAccountTransactionsServiceTest {

    private static final AccountId ACCOUNT_ID =
            new AccountId(UUID.fromString("0feb849d-4a14-469b-af56-7195ee2e5c80"));

    @Test
    void returnsStableNewestFirstCursorPages() {
        InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
        InMemoryLedgerRepository ledgerRepository = new InMemoryLedgerRepository();
        accountRepository.save(BankAccount.restore(
                ACCOUNT_ID,
                CustomerId.generate(),
                Money.of("30.00", "USD"),
                AccountStatus.ACTIVE));
        LedgerEntry oldest = entry(
                "00000000-0000-0000-0000-000000000001",
                "2026-07-23T10:00:00Z",
                "10.00");
        LedgerEntry second = entry(
                "00000000-0000-0000-0000-000000000002",
                "2026-07-23T11:00:00Z",
                "20.00");
        LedgerEntry newest = entry(
                "00000000-0000-0000-0000-000000000003",
                "2026-07-23T11:00:00Z",
                "30.00");
        ledgerRepository.appendAll(java.util.List.of(oldest, second, newest));
        GetAccountTransactionsService service =
                new GetAccountTransactionsService(accountRepository, ledgerRepository);

        AccountTransactionPage firstPage = service.getTransactions(
                new GetAccountTransactionsQuery(ACCOUNT_ID, 2, null));
        AccountTransactionPage secondPage = service.getTransactions(
                new GetAccountTransactionsQuery(
                        ACCOUNT_ID,
                        2,
                        firstPage.nextPosition()));

        assertEquals(
                java.util.List.of(newest.id(), second.id()),
                firstPage.transactions().stream()
                        .map(transaction -> transaction.ledgerEntryId())
                        .toList());
        assertEquals(
                java.util.List.of(oldest.id()),
                secondPage.transactions().stream()
                        .map(transaction -> transaction.ledgerEntryId())
                        .toList());
        assertNull(secondPage.nextPosition());
    }

    private static LedgerEntry entry(
            String entryId,
            String occurredAt,
            String balanceAfter) {
        UUID id = UUID.fromString(entryId);
        return new LedgerEntry(
                new LedgerEntryId(id),
                new LedgerTransactionId(id),
                ACCOUNT_ID,
                LedgerEntryType.DEPOSIT,
                Money.of("10.00", "USD"),
                Money.of(balanceAfter, "USD"),
                Instant.parse(occurredAt),
                "Deposit");
    }
}
