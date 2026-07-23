package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Temporary adapter used until the PostgreSQL repository is introduced.
 */
public final class InMemoryAccountRepository implements AccountRepository {

    private final Map<AccountId, BankAccount> accounts = new HashMap<>();

    @Override
    public synchronized Optional<BankAccount> findById(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return Optional.ofNullable(accounts.get(accountId)).map(this::copy);
    }

    @Override
    public synchronized void save(BankAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        accounts.put(account.id(), copy(account));
    }

    private BankAccount copy(BankAccount account) {
        return BankAccount.restore(
                account.id(),
                account.ownerId(),
                account.balance(),
                account.status());
    }
}

