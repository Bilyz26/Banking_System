package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.application.ConcurrentAccountModificationException;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fast, non-durable adapter used by the default development profile.
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
        BankAccount storedAccount = accounts.get(account.id());
        if (storedAccount != null && storedAccount.version() != account.version()) {
            throw new ConcurrentAccountModificationException(account.id());
        }
        long storedVersion =
                storedAccount == null ? Math.max(0, account.version()) : account.version() + 1;
        accounts.put(account.id(), copy(account, storedVersion));
    }

    public synchronized void saveAll(BankAccount... accountsToSave) {
        Objects.requireNonNull(accountsToSave, "accounts to save must not be null");
        List<BankAccount> snapshots = java.util.Arrays.stream(accountsToSave)
                .map(account -> Objects.requireNonNull(
                        account,
                        "account to save must not be null"))
                .map(account -> copy(account, account.version() + 1))
                .toList();
        for (BankAccount account : accountsToSave) {
            BankAccount storedAccount = accounts.get(account.id());
            if (storedAccount == null || storedAccount.version() != account.version()) {
                throw new ConcurrentAccountModificationException(account.id());
            }
        }
        snapshots.forEach(account -> accounts.put(account.id(), account));
    }

    private BankAccount copy(BankAccount account) {
        return copy(account, account.version());
    }

    private BankAccount copy(BankAccount account, long version) {
        return BankAccount.restore(
                account.id(),
                account.ownerId(),
                account.balance(),
                account.status(),
                version);
    }
}
