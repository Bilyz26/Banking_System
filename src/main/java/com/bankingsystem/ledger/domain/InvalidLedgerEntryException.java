package com.bankingsystem.ledger.domain;

import com.bankingsystem.shared.domain.DomainException;

public final class InvalidLedgerEntryException extends DomainException {

    public InvalidLedgerEntryException(String message) {
        super(message);
    }
}

