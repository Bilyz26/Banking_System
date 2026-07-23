package com.bankingsystem.ledger.application.port.out;

import com.bankingsystem.ledger.domain.LedgerEntryId;

@FunctionalInterface
public interface LedgerEntryIdGenerator {

    LedgerEntryId generate();
}
