package com.bankingsystem.ledger.presentation;

import com.bankingsystem.ledger.application.port.in.AccountTransactionPage;

import java.util.List;

public record AccountTransactionPageResponse(
        List<AccountTransactionResponse> transactions,
        String nextCursor) {

    public AccountTransactionPageResponse {
        transactions = List.copyOf(transactions);
    }

    static AccountTransactionPageResponse from(AccountTransactionPage page) {
        return new AccountTransactionPageResponse(
                page.transactions().stream()
                        .map(AccountTransactionResponse::from)
                        .toList(),
                TransactionCursorCodec.encode(page.nextPosition()));
    }
}
