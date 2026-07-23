package com.bankingsystem.transfer.application.port.out;

import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.domain.TransferLedgerEntries;

/**
 * Persists both account states and both ledger entries as one unit.
 */
@FunctionalInterface
public interface TransferCommitter {

    void commit(
            BankAccount sourceAccount,
            BankAccount destinationAccount,
            TransferLedgerEntries ledgerEntries);
}
