package com.bankingsystem.ledger.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Temporary append-only adapter used until PostgreSQL is introduced.
 */
public final class InMemoryLedgerRepository implements LedgerRepository {

    private final Map<LedgerEntryId, LedgerEntry> entriesById = new HashMap<>();
    private final Map<AccountId, List<LedgerEntry>> entriesByAccountId = new HashMap<>();

    @Override
    public synchronized void append(LedgerEntry ledgerEntry) {
        Objects.requireNonNull(ledgerEntry, "ledger entry must not be null");
        if (entriesById.containsKey(ledgerEntry.id())) {
            throw new DuplicateLedgerEntryException(ledgerEntry.id());
        }

        entriesById.put(ledgerEntry.id(), ledgerEntry);
        entriesByAccountId
                .computeIfAbsent(ledgerEntry.accountId(), ignored -> new ArrayList<>())
                .add(ledgerEntry);
    }

    @Override
    public synchronized List<LedgerEntry> findByAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return List.copyOf(entriesByAccountId.getOrDefault(accountId, List.of()));
    }
}
