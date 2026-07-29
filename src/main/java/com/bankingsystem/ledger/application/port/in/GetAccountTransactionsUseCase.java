package com.bankingsystem.ledger.application.port.in;

@FunctionalInterface
public interface GetAccountTransactionsUseCase {

    AccountTransactionPage getTransactions(GetAccountTransactionsQuery query);
}
