package com.bankingsystem.ledger.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Correlates all ledger entries produced by one completed banking operation.
 */
public record LedgerTransactionId(UUID value) {

    public LedgerTransactionId {
        Objects.requireNonNull(value, "ledger transaction id must not be null");
    }
}

