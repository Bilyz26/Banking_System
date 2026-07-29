package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.DepositMoneyCommand;
import com.bankingsystem.account.application.port.in.MoneyOperationResult;
import com.bankingsystem.account.application.port.in.WithdrawMoneyCommand;
import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.domain.InsufficientFundsException;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.infrastructure.InMemoryLedgerRepository;
import com.bankingsystem.shared.application.IdempotencyConflictException;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountMoneyOperationServiceTest {

    private static final AccountId ACCOUNT_ID =
            new AccountId(UUID.fromString("4261d99d-9ba9-45e0-b55a-c7250f305e05"));
    private static final LedgerEntryId ENTRY_ID =
            new LedgerEntryId(UUID.fromString("f4a93ed2-79be-49fb-9886-abc322595d1b"));
    private static final LedgerTransactionId TRANSACTION_ID =
            new LedgerTransactionId(
                    UUID.fromString("848c8477-755d-466e-a6cb-e950e12acbcb"));
    private static final Instant NOW = Instant.parse("2026-07-23T12:00:00Z");

    private final TestAccountRepository repository = new TestAccountRepository();
    private final InMemoryLedgerRepository ledgerRepository = new InMemoryLedgerRepository();
    private final CapturingCommitter committer =
            new CapturingCommitter(repository, ledgerRepository);
    private final AccountMoneyOperationService service = new AccountMoneyOperationService(
            repository,
            committer,
            () -> ENTRY_ID,
            () -> TRANSACTION_ID,
            ledgerRepository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void depositsMoneyAndCreatesCreditLedgerEntry() {
        repository.save(openAccount(Money.of("10.00", "USD")));

        MoneyOperationResult result = service.deposit(
                new DepositMoneyCommand(
                        ACCOUNT_ID,
                        Money.of("25.00", "USD"),
                        "Cash deposit"));

        assertEquals(Money.of("35.00", "USD"), result.balance());
        assertEquals(ENTRY_ID, result.ledgerEntryId());
        assertEquals(TRANSACTION_ID, result.transactionId());
        assertEquals(LedgerEntryType.DEPOSIT, committer.ledgerEntry.type());
        assertEquals(Money.of("35.00", "USD"), committer.ledgerEntry.balanceAfter());
        assertEquals(NOW, committer.ledgerEntry.occurredAt());
    }

    @Test
    void withdrawsMoneyAndCreatesDebitLedgerEntry() {
        repository.save(openAccount(Money.of("100.00", "USD")));

        MoneyOperationResult result = service.withdraw(
                new WithdrawMoneyCommand(
                        ACCOUNT_ID,
                        Money.of("40.00", "USD"),
                        "ATM withdrawal"));

        assertEquals(Money.of("60.00", "USD"), result.balance());
        assertEquals(LedgerEntryType.WITHDRAWAL, committer.ledgerEntry.type());
        assertEquals(Money.of("40.00", "USD"), committer.ledgerEntry.amount());
    }

    @Test
    void failedWithdrawalDoesNotCommitAccountOrLedger() {
        repository.save(openAccount(Money.of("10.00", "USD")));

        assertThrows(
                InsufficientFundsException.class,
                () -> service.withdraw(
                        new WithdrawMoneyCommand(
                                ACCOUNT_ID,
                                Money.of("11.00", "USD"),
                                "Invalid withdrawal")));

        assertNull(committer.ledgerEntry);
        assertEquals(
                Money.of("10.00", "USD"),
                repository.findById(ACCOUNT_ID).orElseThrow().balance());
    }

    @Test
    void rejectsUnknownAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> service.deposit(
                        new DepositMoneyCommand(
                                ACCOUNT_ID,
                                Money.of("10.00", "USD"),
                                "Unknown account")));
        assertNull(committer.ledgerEntry);
    }

    @Test
    void returnsOriginalDepositWhenIdempotencyKeyIsRetried() {
        repository.save(openAccount(Money.of("10.00", "USD")));
        DepositMoneyCommand command = new DepositMoneyCommand(
                ACCOUNT_ID,
                Money.of("25.00", "USD"),
                "Cash deposit",
                TRANSACTION_ID);

        MoneyOperationResult firstResult = service.deposit(command);
        MoneyOperationResult retriedResult = service.deposit(command);

        assertEquals(firstResult, retriedResult);
        assertEquals(
                Money.of("35.00", "USD"),
                repository.findById(ACCOUNT_ID).orElseThrow().balance());
        assertEquals(1, ledgerRepository.findByTransactionId(TRANSACTION_ID).size());
    }

    @Test
    void rejectsIdempotencyKeyReusedForDifferentAmount() {
        repository.save(openAccount(Money.of("10.00", "USD")));
        service.deposit(new DepositMoneyCommand(
                ACCOUNT_ID,
                Money.of("25.00", "USD"),
                "Cash deposit",
                TRANSACTION_ID));

        assertThrows(
                IdempotencyConflictException.class,
                () -> service.deposit(new DepositMoneyCommand(
                        ACCOUNT_ID,
                        Money.of("30.00", "USD"),
                        "Cash deposit",
                        TRANSACTION_ID)));
    }

    private static BankAccount openAccount(Money balance) {
        return BankAccount.restore(
                ACCOUNT_ID,
                CustomerId.generate(),
                balance,
                com.bankingsystem.account.domain.AccountStatus.ACTIVE);
    }

    private static final class TestAccountRepository implements AccountRepository {

        private final Map<AccountId, BankAccount> accounts = new HashMap<>();

        @Override
        public Optional<BankAccount> findById(AccountId accountId) {
            return Optional.ofNullable(accounts.get(accountId));
        }

        @Override
        public void save(BankAccount account) {
            accounts.put(account.id(), account);
        }
    }

    private static final class CapturingCommitter implements AccountOperationCommitter {

        private final AccountRepository repository;
        private final InMemoryLedgerRepository ledgerRepository;
        private LedgerEntry ledgerEntry;

        private CapturingCommitter(
                AccountRepository repository,
                InMemoryLedgerRepository ledgerRepository) {
            this.repository = repository;
            this.ledgerRepository = ledgerRepository;
        }

        @Override
        public void commit(BankAccount account, LedgerEntry ledgerEntry) {
            this.ledgerEntry = ledgerEntry;
            ledgerRepository.append(ledgerEntry);
            repository.save(account);
        }
    }
}
