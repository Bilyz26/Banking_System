package com.bankingsystem.ledger.application;

import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.shared.application.ApplicationException;

public final class DuplicateLedgerEntryException extends ApplicationException {

    public DuplicateLedgerEntryException(LedgerEntryId ledgerEntryId) {
        super("Ledger entry '%s' already exists".formatted(ledgerEntryId.value()));
    }
}
