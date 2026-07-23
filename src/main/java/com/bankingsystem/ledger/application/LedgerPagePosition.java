package com.bankingsystem.ledger.application;

import com.bankingsystem.ledger.domain.LedgerEntryId;

import java.time.Instant;
import java.util.Objects;

public record LedgerPagePosition(
        Instant occurredAt,
        LedgerEntryId entryId) {

    public LedgerPagePosition {
        Objects.requireNonNull(occurredAt, "cursor occurrence time must not be null");
        Objects.requireNonNull(entryId, "cursor ledger entry id must not be null");
    }
}
