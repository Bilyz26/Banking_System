package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.application.ConcurrentAccountModificationException;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.Money;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JdbcAccountRepository implements AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbc template must not be null");
    }

    @Override
    public Optional<BankAccount> findById(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return jdbcTemplate.query(
                        """
                        SELECT account_id, owner_id, balance, currency_code, status, version
                        FROM accounts
                        WHERE account_id = ?
                        """,
                        (resultSet, rowNumber) -> BankAccount.restore(
                                new AccountId(resultSet.getObject("account_id", UUID.class)),
                                new CustomerId(resultSet.getObject("owner_id", UUID.class)),
                                new Money(
                                        resultSet.getBigDecimal("balance"),
                                        Currency.getInstance(resultSet.getString("currency_code"))),
                                AccountStatus.valueOf(resultSet.getString("status")),
                                resultSet.getLong("version")),
                        accountId.value())
                .stream()
                .findFirst();
    }

    @Override
    public void save(BankAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        if (account.version() == -1) {
            insert(account, 0);
            return;
        }

        int updatedRows = jdbcTemplate.update(
                """
                UPDATE accounts
                SET balance = ?, status = ?, version = version + 1
                WHERE account_id = ? AND version = ?
                """,
                account.balance().amount(),
                account.status().name(),
                account.id().value(),
                account.version());
        if (updatedRows == 1) {
            return;
        }
        if (exists(account.id())) {
            throw new ConcurrentAccountModificationException(account.id());
        }

        insert(account, account.version());
    }

    private boolean exists(AccountId accountId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE account_id = ?",
                Integer.class,
                accountId.value());
        return count != null && count > 0;
    }

    private void insert(BankAccount account, long storedVersion) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO accounts (
                        account_id, owner_id, balance, currency_code, status, version
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    account.id().value(),
                    account.ownerId().value(),
                    account.balance().amount(),
                    account.balance().currency().getCurrencyCode(),
                    account.status().name(),
                    storedVersion);
        } catch (DuplicateKeyException exception) {
            throw new ConcurrentAccountModificationException(account.id());
        }
    }
}
