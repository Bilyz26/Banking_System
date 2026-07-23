package com.bankingsystem.ledger.application.port.in;

import com.bankingsystem.ledger.application.LedgerPagePosition;

import java.util.List;

public record AccountTransactionPage(
        List<AccountTransactionResult> transactions,
        LedgerPagePosition nextPosition) {

    public AccountTransactionPage {
        transactions = List.copyOf(transactions);
    }
}
