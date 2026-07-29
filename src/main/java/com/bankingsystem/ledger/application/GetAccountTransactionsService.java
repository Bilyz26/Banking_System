package com.bankingsystem.ledger.application;

import com.bankingsystem.account.application.AccountNotFoundException;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.ledger.application.port.in.AccountTransactionPage;
import com.bankingsystem.ledger.application.port.in.AccountTransactionResult;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsQuery;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsUseCase;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;

import java.util.List;
import java.util.Objects;

public final class GetAccountTransactionsService
        implements GetAccountTransactionsUseCase {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public GetAccountTransactionsService(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.ledgerRepository =
                Objects.requireNonNull(ledgerRepository, "ledger repository must not be null");
    }

    @Override
    public AccountTransactionPage getTransactions(
            GetAccountTransactionsQuery query) {
        Objects.requireNonNull(query, "transaction query must not be null");
        if (accountRepository.findById(query.accountId()).isEmpty()) {
            throw new AccountNotFoundException(query.accountId());
        }

        List<LedgerEntry> fetched = ledgerRepository.findPageByAccountId(
                query.accountId(),
                query.position(),
                query.limit() + 1);
        boolean hasMore = fetched.size() > query.limit();
        List<LedgerEntry> pageEntries =
                hasMore ? fetched.subList(0, query.limit()) : fetched;
        LedgerPagePosition nextPosition = hasMore
                ? positionOf(pageEntries.getLast())
                : null;

        return new AccountTransactionPage(
                pageEntries.stream()
                        .map(GetAccountTransactionsService::toResult)
                        .toList(),
                nextPosition);
    }

    private static AccountTransactionResult toResult(LedgerEntry entry) {
        return new AccountTransactionResult(
                entry.id(),
                entry.transactionId(),
                entry.accountId(),
                entry.type(),
                entry.amount(),
                entry.balanceAfter(),
                entry.occurredAt(),
                entry.description());
    }

    private static LedgerPagePosition positionOf(LedgerEntry entry) {
        return new LedgerPagePosition(entry.occurredAt(), entry.id());
    }
}
