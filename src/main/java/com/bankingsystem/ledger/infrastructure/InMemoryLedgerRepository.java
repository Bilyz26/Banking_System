package com.bankingsystem.ledger.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.DuplicateLedgerEntryException;
import com.bankingsystem.ledger.application.LedgerPagePosition;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.domain.LedgerEntryType;

import java.util.ArrayList;
import java.util.Comparator;
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

    private static final Comparator<LedgerEntry> NEWEST_FIRST =
            Comparator.comparing(LedgerEntry::occurredAt)
                    .thenComparing(entry -> entry.id().value())
                    .reversed();

    private final Map<LedgerEntryId, LedgerEntry> entriesById = new HashMap<>();
    private final Map<AccountId, List<LedgerEntry>> entriesByAccountId = new HashMap<>();
    private final Map<LedgerTransactionId, List<LedgerEntry>> entriesByTransactionId =
            new HashMap<>();
    private final Set<TransactionEntryType> transactionEntryTypes = new HashSet<>();

    @Override
    public synchronized void append(LedgerEntry ledgerEntry) {
        appendAll(List.of(ledgerEntry));
    }

    @Override
    public synchronized void appendAll(List<LedgerEntry> ledgerEntries) {
        Objects.requireNonNull(ledgerEntries, "ledger entries must not be null");
        List<LedgerEntry> entries = List.copyOf(ledgerEntries);
        Set<LedgerEntryId> batchEntryIds = new HashSet<>();
        Set<TransactionEntryType> batchTransactionEntryTypes = new HashSet<>();

        for (LedgerEntry entry : entries) {
            TransactionEntryType transactionEntryType =
                    new TransactionEntryType(entry.transactionId(), entry.type());
            if (entriesById.containsKey(entry.id())
                    || !batchEntryIds.add(entry.id())
                    || transactionEntryTypes.contains(transactionEntryType)
                    || !batchTransactionEntryTypes.add(transactionEntryType)) {
                throw new DuplicateLedgerEntryException(entry.id());
            }
        }

        for (LedgerEntry entry : entries) {
            entriesById.put(entry.id(), entry);
            entriesByAccountId
                    .computeIfAbsent(entry.accountId(), ignored -> new ArrayList<>())
                    .add(entry);
            entriesByTransactionId
                    .computeIfAbsent(entry.transactionId(), ignored -> new ArrayList<>())
                    .add(entry);
            transactionEntryTypes.add(
                    new TransactionEntryType(entry.transactionId(), entry.type()));
        }
    }

    @Override
    public synchronized List<LedgerEntry> findByAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return List.copyOf(entriesByAccountId.getOrDefault(accountId, List.of()));
    }

    @Override
    public synchronized List<LedgerEntry> findByTransactionId(
            LedgerTransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transaction id must not be null");
        return List.copyOf(entriesByTransactionId.getOrDefault(transactionId, List.of()));
    }

    @Override
    public synchronized List<LedgerEntry> findPageByAccountId(
            AccountId accountId,
            LedgerPagePosition position,
            int limit) {
        Objects.requireNonNull(accountId, "account id must not be null");
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be greater than zero");
        }
        return entriesByAccountId.getOrDefault(accountId, List.of()).stream()
                .filter(entry -> isAfterCursor(entry, position))
                .sorted(NEWEST_FIRST)
                .limit(limit)
                .toList();
    }

    private static boolean isAfterCursor(
            LedgerEntry entry,
            LedgerPagePosition position) {
        if (position == null) {
            return true;
        }
        int timeComparison = entry.occurredAt().compareTo(position.occurredAt());
        return timeComparison < 0
                || (timeComparison == 0
                && entry.id().value().compareTo(position.entryId().value()) < 0);
    }

    private record TransactionEntryType(
            LedgerTransactionId transactionId,
            LedgerEntryType entryType) {
    }
}
