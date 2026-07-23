package com.bankingsystem.ledger.application.port.out;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerEntry;

import java.util.List;

public interface LedgerRepository {

    void append(LedgerEntry ledgerEntry);

    List<LedgerEntry> findByAccountId(AccountId accountId);
}
