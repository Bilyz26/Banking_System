package com.bankingsystem.ledger.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fast, non-durable append-only adapter used by the default development profile.
 */
public final class InMemoryLedgerRepository implements LedgerRepository {

    private final Map<LedgerEntryId, LedgerEntry> entriesById = new HashMap<>();
    private final Map<AccountId, List<LedgerEntry>> entriesByAccountId = new HashMap<>();

    @Override
    public synchronized void append(LedgerEntry ledgerEntry) {
        appendAll(List.of(ledgerEntry));
    }

    @Override
    public synchronized void appendAll(List<LedgerEntry> ledgerEntries) {
        Objects.requireNonNull(ledgerEntries, "ledger entries must not be null");
        List<LedgerEntry> entries = List.copyOf(ledgerEntries);
        Set<LedgerEntryId> batchEntryIds = new HashSet<>();

        for (LedgerEntry entry : entries) {
            if (entriesById.containsKey(entry.id()) || !batchEntryIds.add(entry.id())) {
                throw new DuplicateLedgerEntryException(entry.id());
            }
        }

        for (LedgerEntry entry : entries) {
            entriesById.put(entry.id(), entry);
            entriesByAccountId
                    .computeIfAbsent(entry.accountId(), ignored -> new ArrayList<>())
                    .add(entry);
        }
    }

    @Override
    public synchronized List<LedgerEntry> findByAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return List.copyOf(entriesByAccountId.getOrDefault(accountId, List.of()));
    }
}
