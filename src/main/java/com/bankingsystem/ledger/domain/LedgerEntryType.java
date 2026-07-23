package com.bankingsystem.ledger.domain;

public enum LedgerEntryType {
    DEPOSIT(LedgerDirection.CREDIT),
    WITHDRAWAL(LedgerDirection.DEBIT),
    TRANSFER_CREDIT(LedgerDirection.CREDIT),
    TRANSFER_DEBIT(LedgerDirection.DEBIT);

    private final LedgerDirection direction;

    LedgerEntryType(LedgerDirection direction) {
        this.direction = direction;
    }

    public LedgerDirection direction() {
        return direction;
    }
}

