package com.bankingsystem.persistence;

import com.bankingsystem.account.application.ConcurrentAccountModificationException;
import com.bankingsystem.account.application.port.in.DepositMoneyCommand;
import com.bankingsystem.account.application.port.in.DepositMoneyUseCase;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.domain.TransferLedgerEntries;
import com.bankingsystem.shared.domain.Money;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;
import com.bankingsystem.transfer.domain.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = BankingSystemApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:banking;MODE=PostgreSQL;"
                        + "DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.flyway.enabled=true"
        })
@ActiveProfiles("postgresql")
class PostgresqlPersistenceIntegrationTest {

    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("31b48893-f7e9-44cf-ae54-1c40b6a136fb"));
    private static final AccountId SOURCE_ACCOUNT_ID =
            new AccountId(UUID.fromString("72cd3e62-b1f0-4b92-b8bd-803229c5cd0a"));
    private static final AccountId DESTINATION_ACCOUNT_ID =
            new AccountId(UUID.fromString("1b51b628-3f93-407b-ad77-e0a4acafe283"));

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private DepositMoneyUseCase depositMoneyUseCase;

    @Autowired
    private TransferCommitter transferCommitter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM ledger_entries");
        jdbcTemplate.update("DELETE FROM accounts");
        jdbcTemplate.update("DELETE FROM customers");
        customerRepository.save(new Customer(
                CUSTOMER_ID,
                "Database Test Customer",
                "database@example.com"));
    }

    @Test
    void persistsAccountOperationAndImmutableLedgerEntry() {
        accountRepository.save(account(SOURCE_ACCOUNT_ID, "0.00"));

        depositMoneyUseCase.deposit(new DepositMoneyCommand(
                SOURCE_ACCOUNT_ID,
                Money.of("25.00", "USD"),
                "Persistent deposit"));

        BankAccount storedAccount =
                accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow();
        assertEquals(Money.of("25.00", "USD"), storedAccount.balance());
        assertEquals(1, storedAccount.version());

        LedgerEntry storedEntry =
                ledgerRepository.findByAccountId(SOURCE_ACCOUNT_ID).getFirst();
        assertEquals(LedgerEntryType.DEPOSIT, storedEntry.type());
        assertEquals(Money.of("25.00", "USD"), storedEntry.balanceAfter());
    }

    @Test
    void rejectsStaleAccountUpdate() {
        accountRepository.save(account(SOURCE_ACCOUNT_ID, "0.00"));
        BankAccount firstCopy = accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow();
        BankAccount staleCopy = accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow();
        firstCopy.deposit(Money.of("10.00", "USD"));
        staleCopy.deposit(Money.of("5.00", "USD"));

        accountRepository.save(firstCopy);

        assertThrows(
                ConcurrentAccountModificationException.class,
                () -> accountRepository.save(staleCopy));
        assertEquals(
                Money.of("10.00", "USD"),
                accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow().balance());
    }

    @Test
    void rejectsASecondNewAccountWithTheSameIdentity() {
        accountRepository.save(BankAccount.open(
                SOURCE_ACCOUNT_ID,
                CUSTOMER_ID,
                Currency.getInstance("USD")));

        BankAccount collidingAccount = BankAccount.open(
                SOURCE_ACCOUNT_ID,
                CUSTOMER_ID,
                Currency.getInstance("USD"));

        assertThrows(
                ConcurrentAccountModificationException.class,
                () -> accountRepository.save(collidingAccount));
    }

    @Test
    void rollsBackBothAccountsWhenLedgerBatchFails() {
        accountRepository.save(account(SOURCE_ACCOUNT_ID, "100.00"));
        accountRepository.save(account(DESTINATION_ACCOUNT_ID, "0.00"));
        LedgerEntry existingEntry = ledgerEntry(
                new LedgerEntryId(
                        UUID.fromString("ca45f639-5f23-4ff8-b908-d352cc573b8d")),
                SOURCE_ACCOUNT_ID,
                Money.of("100.00", "USD"));
        ledgerRepository.append(existingEntry);

        BankAccount sourceAccount =
                accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow();
        BankAccount destinationAccount =
                accountRepository.findById(DESTINATION_ACCOUNT_ID).orElseThrow();
        Money transferAmount = Money.of("40.00", "USD");
        new TransferService().transfer(sourceAccount, destinationAccount, transferAmount);

        TransferLedgerEntries entries = TransferLedgerEntries.posted(
                new LedgerEntryId(
                        UUID.fromString("105a835f-e945-4a5b-8602-b97eb91d3f7c")),
                existingEntry.id(),
                new LedgerTransactionId(
                        UUID.fromString("12576b89-2e9e-40d2-bd86-dd1996ec8209")),
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                transferAmount,
                sourceAccount.balance(),
                destinationAccount.balance(),
                Instant.parse("2026-07-23T21:00:00Z"),
                "Rollback test");

        assertThrows(
                DuplicateLedgerEntryException.class,
                () -> transferCommitter.commit(sourceAccount, destinationAccount, entries));

        assertEquals(
                Money.of("100.00", "USD"),
                accountRepository.findById(SOURCE_ACCOUNT_ID).orElseThrow().balance());
        assertEquals(
                Money.of("0.00", "USD"),
                accountRepository.findById(DESTINATION_ACCOUNT_ID).orElseThrow().balance());
        assertEquals(1, ledgerRepository.findByAccountId(SOURCE_ACCOUNT_ID).size());
        assertEquals(0, ledgerRepository.findByAccountId(DESTINATION_ACCOUNT_ID).size());
    }

    private static BankAccount account(AccountId accountId, String balance) {
        return BankAccount.restore(
                accountId,
                CUSTOMER_ID,
                Money.of(balance, "USD"),
                AccountStatus.ACTIVE);
    }

    private static LedgerEntry ledgerEntry(
            LedgerEntryId entryId,
            AccountId accountId,
            Money balanceAfter) {
        return new LedgerEntry(
                entryId,
                new LedgerTransactionId(
                        UUID.fromString("712d06a7-f4db-4212-88b0-f9e8d06976a1")),
                accountId,
                LedgerEntryType.DEPOSIT,
                Money.of("100.00", "USD"),
                balanceAfter,
                Instant.parse("2026-07-23T20:00:00Z"),
                "Existing entry");
    }
}
