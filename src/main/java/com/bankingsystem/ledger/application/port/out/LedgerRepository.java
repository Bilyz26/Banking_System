package com.bankingsystem.ledger.application.port.out;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerTransactionId;

import java.util.List;

public interface LedgerRepository {

    void append(LedgerEntry ledgerEntry);

    /**
     * Appends every entry or none of them.
     */
    void appendAll(List<LedgerEntry> ledgerEntries);

    List<LedgerEntry> findByAccountId(AccountId accountId);

    List<LedgerEntry> findByTransactionId(LedgerTransactionId transactionId);
}
