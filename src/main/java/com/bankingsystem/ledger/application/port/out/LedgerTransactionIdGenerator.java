package com.bankingsystem.ledger.application.port.out;

import com.bankingsystem.ledger.domain.LedgerTransactionId;

@FunctionalInterface
public interface LedgerTransactionIdGenerator {

    LedgerTransactionId generate();
}
