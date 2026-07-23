package com.bankingsystem.ledger.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.LedgerPagePosition;

import java.util.Objects;

public record GetAccountTransactionsQuery(
        AccountId accountId,
        int limit,
        LedgerPagePosition position) {

    public static final int MAXIMUM_PAGE_SIZE = 100;

    public GetAccountTransactionsQuery {
        Objects.requireNonNull(accountId, "account id must not be null");
        if (limit < 1 || limit > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "limit must be between 1 and " + MAXIMUM_PAGE_SIZE);
        }
    }
}
