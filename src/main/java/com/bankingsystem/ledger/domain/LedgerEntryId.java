package com.bankingsystem.ledger.domain;

import java.util.Objects;
import java.util.UUID;

public record LedgerEntryId(UUID value) {

    public LedgerEntryId {
        Objects.requireNonNull(value, "ledger entry id must not be null");
    }
}

