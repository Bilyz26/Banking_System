package com.bankingsystem.transfer.application;

import com.bankingsystem.account.application.AccountNotFoundException;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.domain.InsufficientFundsException;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.infrastructure.InMemoryLedgerRepository;
import com.bankingsystem.shared.domain.Money;
import com.bankingsystem.shared.application.IdempotencyConflictException;
import com.bankingsystem.transfer.application.port.in.TransferMoneyCommand;
import com.bankingsystem.transfer.application.port.in.TransferMoneyResult;
import com.bankingsystem.transfer.domain.TransferService;
import com.bankingsystem.transfer.infrastructure.InMemoryTransferCommitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferMoneyServiceTest {

    private static final AccountId SOURCE_ACCOUNT_ID =
            accountId("a5e4a105-03b1-407d-b823-df4732ec20c7");
    private static final AccountId DESTINATION_ACCOUNT_ID =
            accountId("d06bb1ae-072b-401f-be1c-611d82389648");
    private static final LedgerEntryId DEBIT_ENTRY_ID =
            ledgerEntryId("98df5ab7-8389-48f2-8555-d23041b53074");
    private static final LedgerEntryId CREDIT_ENTRY_ID =
            ledgerEntryId("14c4642a-2ef4-44a4-8237-3293171595f4");
    private static final LedgerTransactionId TRANSACTION_ID =
            new LedgerTransactionId(
                    UUID.fromString("8c2aa059-857c-4b58-b5a3-a0170029eb46"));
    private static final Instant OCCURRED_AT = Instant.parse("2026-07-23T20:00:00Z");

    private InMemoryAccountRepository accountRepository;
    private InMemoryLedgerRepository ledgerRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        ledgerRepository = new InMemoryLedgerRepository();
        accountRepository.save(account(SOURCE_ACCOUNT_ID, "100.00"));
        accountRepository.save(account(DESTINATION_ACCOUNT_ID, "20.00"));
    }

    @Test
    void transfersMoneyAndPublishesBalancedLedgerEntries() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, CREDIT_ENTRY_ID);

        TransferMoneyResult result = service.transfer(new TransferMoneyCommand(
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                Money.of("35.00", "USD"),
                "Invoice 1042"));

        assertEquals(Money.of("65.00", "USD"), result.sourceBalance());
        assertEquals(Money.of("55.00", "USD"), result.destinationBalance());
        assertEquals(TRANSACTION_ID, result.transactionId());

        LedgerEntry debitEntry =
                ledgerRepository.findByAccountId(SOURCE_ACCOUNT_ID).getFirst();
        LedgerEntry creditEntry =
                ledgerRepository.findByAccountId(DESTINATION_ACCOUNT_ID).getFirst();
        assertEquals(LedgerEntryType.TRANSFER_DEBIT, debitEntry.type());
        assertEquals(LedgerEntryType.TRANSFER_CREDIT, creditEntry.type());
        assertEquals(debitEntry.transactionId(), creditEntry.transactionId());
        assertEquals(debitEntry.amount(), creditEntry.amount());
        assertEquals(OCCURRED_AT, debitEntry.occurredAt());
    }

    @Test
    void unknownDestinationDoesNotChangeSourceOrLedger() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, CREDIT_ENTRY_ID);
        AccountId unknownAccountId =
                accountId("986af299-7349-423a-9336-81954ac18654");

        assertThrows(
                AccountNotFoundException.class,
                () -> service.transfer(new TransferMoneyCommand(
                        SOURCE_ACCOUNT_ID,
                        unknownAccountId,
                        Money.of("35.00", "USD"),
                        "Invalid destination")));

        assertUnchangedBalancesAndEmptyLedger();
    }

    @Test
    void insufficientFundsDoNotChangeAccountsOrLedger() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, CREDIT_ENTRY_ID);

        assertThrows(
                InsufficientFundsException.class,
                () -> service.transfer(new TransferMoneyCommand(
                        SOURCE_ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        Money.of("101.00", "USD"),
                        "Too large")));

        assertUnchangedBalancesAndEmptyLedger();
    }

    @Test
    void duplicateEntryIdentityPublishesNeitherAccountChange() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, DEBIT_ENTRY_ID);

        assertThrows(
                DuplicateLedgerEntryException.class,
                () -> service.transfer(new TransferMoneyCommand(
                        SOURCE_ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        Money.of("35.00", "USD"),
                        "Duplicate entry IDs")));

        assertUnchangedBalancesAndEmptyLedger();
    }

    @Test
    void returnsOriginalTransferWhenIdempotencyKeyIsRetried() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, CREDIT_ENTRY_ID);
        TransferMoneyCommand command = new TransferMoneyCommand(
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                Money.of("35.00", "USD"),
                "Invoice 1042",
                TRANSACTION_ID);

        TransferMoneyResult firstResult = service.transfer(command);
        TransferMoneyResult retriedResult = service.transfer(command);

        assertEquals(firstResult, retriedResult);
        assertEquals(Money.of("65.00", "USD"), retriedResult.sourceBalance());
        assertEquals(Money.of("55.00", "USD"), retriedResult.destinationBalance());
    }

    @Test
    void rejectsIdempotencyKeyReusedForDifferentTransfer() {
        TransferMoneyService service = transferService(DEBIT_ENTRY_ID, CREDIT_ENTRY_ID);
        service.transfer(new TransferMoneyCommand(
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                Money.of("35.00", "USD"),
                "Invoice 1042",
                TRANSACTION_ID));

        assertThrows(
                IdempotencyConflictException.class,
                () -> service.transfer(new TransferMoneyCommand(
                        SOURCE_ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        Money.of("36.00", "USD"),
                        "Invoice 1042",
                        TRANSACTION_ID)));
    }

    private TransferMoneyService transferService(LedgerEntryId... generatedEntryIds) {
        Iterator<LedgerEntryId> entryIds = List.of(generatedEntryIds).iterator();
        return new TransferMoneyService(
                accountRepository,
                new TransferService(),
                new InMemoryTransferCommitter(accountRepository, ledgerRepository),
                entryIds::next,
                () -> TRANSACTION_ID,
                ledgerRepository,
                Clock.fixed(OCCURRED_AT, ZoneOffset.UTC));
    }

    private void assertUnchangedBalancesAndEmptyLedger() {
        assertEquals(
                Money.of("100.00", "USD"),
                accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow().balance());
        assertEquals(
                Money.of("20.00", "USD"),
                accountRepository.findById(DESTINATION_ACCOUNT_ID).orElseThrow().balance());
        assertEquals(List.of(), ledgerRepository.findByAccountId(SOURCE_ACCOUNT_ID));
        assertEquals(List.of(), ledgerRepository.findByAccountId(DESTINATION_ACCOUNT_ID));
    }

    private static BankAccount account(AccountId accountId, String balance) {
        return BankAccount.restore(
                accountId,
                CustomerId.generate(),
                Money.of(balance, "USD"),
                AccountStatus.ACTIVE);
    }

    private static AccountId accountId(String value) {
        return new AccountId(UUID.fromString(value));
    }

    private static LedgerEntryId ledgerEntryId(String value) {
        return new LedgerEntryId(UUID.fromString(value));
    }
}
