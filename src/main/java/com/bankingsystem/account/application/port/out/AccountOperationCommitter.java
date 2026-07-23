package com.bankingsystem.account.application.port.out;

import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.domain.LedgerEntry;

/**
 * Atomically persists an account state change and its ledger fact.
 */
@FunctionalInterface
public interface AccountOperationCommitter {

    void commit(BankAccount account, LedgerEntry ledgerEntry);
}
