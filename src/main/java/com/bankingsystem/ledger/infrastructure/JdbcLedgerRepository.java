package com.bankingsystem.ledger.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.application.LedgerPagePosition;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JdbcLedgerRepository implements LedgerRepository {

    private static final String ENTRY_SELECT = """
            SELECT ledger_entry_id, transaction_id, ledger_entries.account_id, entry_type,
                   amount, balance_after, occurred_at, description, accounts.currency_code
            FROM ledger_entries
            JOIN accounts ON accounts.account_id = ledger_entries.account_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public JdbcLedgerRepository(
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbc template must not be null");
        this.transactionTemplate =
                Objects.requireNonNull(transactionTemplate, "transaction template must not be null");
    }

    @Override
    public void append(LedgerEntry ledgerEntry) {
        appendAll(List.of(ledgerEntry));
    }

    @Override
    public void appendAll(List<LedgerEntry> ledgerEntries) {
        Objects.requireNonNull(ledgerEntries, "ledger entries must not be null");
        List<LedgerEntry> entries = List.copyOf(ledgerEntries);
        if (entries.isEmpty()) {
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status ->
                    entries.forEach(this::insert));
        } catch (DuplicateKeyException exception) {
            throw new DuplicateLedgerEntryException(entries.getFirst().id());
        }
    }

    @Override
    public List<LedgerEntry> findByAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return jdbcTemplate.query(
                ENTRY_SELECT + """
                        WHERE ledger_entries.account_id = ?
                        ORDER BY occurred_at, ledger_entry_id
                        """,
                entryRowMapper(),
                accountId.value());
    }

    @Override
    public List<LedgerEntry> findByTransactionId(LedgerTransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transaction id must not be null");
        return jdbcTemplate.query(
                ENTRY_SELECT + """
                        WHERE transaction_id = ?
                        ORDER BY entry_type, ledger_entry_id
                        """,
                entryRowMapper(),
                transactionId.value());
    }

    @Override
    public List<LedgerEntry> findPageByAccountId(
            AccountId accountId,
            LedgerPagePosition position,
            int limit) {
        Objects.requireNonNull(accountId, "account id must not be null");
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be greater than zero");
        }
        if (position == null) {
            return jdbcTemplate.query(
                    ENTRY_SELECT + """
                            WHERE ledger_entries.account_id = ?
                            ORDER BY occurred_at DESC, ledger_entry_id DESC
                            LIMIT ?
                            """,
                    entryRowMapper(),
                    accountId.value(),
                    limit);
        }
        return jdbcTemplate.query(
                ENTRY_SELECT + """
                        WHERE ledger_entries.account_id = ?
                          AND (
                              occurred_at < ?
                              OR (occurred_at = ? AND ledger_entry_id < ?)
                          )
                        ORDER BY occurred_at DESC, ledger_entry_id DESC
                        LIMIT ?
                        """,
                entryRowMapper(),
                accountId.value(),
                Timestamp.from(position.occurredAt()),
                Timestamp.from(position.occurredAt()),
                position.entryId().value(),
                limit);
    }

    private void insert(LedgerEntry entry) {
        jdbcTemplate.update(
                """
                INSERT INTO ledger_entries (
                    ledger_entry_id, transaction_id, account_id, entry_type,
                    amount, balance_after, occurred_at, description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                entry.id().value(),
                entry.transactionId().value(),
                entry.accountId().value(),
                entry.type().name(),
                entry.amount().amount(),
                entry.balanceAfter().amount(),
                Timestamp.from(entry.occurredAt()),
                entry.description());
    }

    private static RowMapper<LedgerEntry> entryRowMapper() {
        return (resultSet, rowNumber) -> new LedgerEntry(
                new LedgerEntryId(resultSet.getObject("ledger_entry_id", UUID.class)),
                new LedgerTransactionId(resultSet.getObject("transaction_id", UUID.class)),
                new AccountId(resultSet.getObject("account_id", UUID.class)),
                LedgerEntryType.valueOf(resultSet.getString("entry_type")),
                new Money(
                        resultSet.getBigDecimal("amount"),
                        Currency.getInstance(resultSet.getString("currency_code"))),
                new Money(
                        resultSet.getBigDecimal("balance_after"),
                        Currency.getInstance(resultSet.getString("currency_code"))),
                resultSet.getTimestamp("occurred_at").toInstant(),
                resultSet.getString("description"));
    }
}
