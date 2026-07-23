package com.bankingsystem.ledger.presentation;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsQuery;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
public final class AccountTransactionController {

    private final GetAccountTransactionsUseCase getAccountTransactionsUseCase;

    public AccountTransactionController(
            GetAccountTransactionsUseCase getAccountTransactionsUseCase) {
        this.getAccountTransactionsUseCase = Objects.requireNonNull(
                getAccountTransactionsUseCase,
                "get account transactions use case must not be null");
    }

    @GetMapping
    public AccountTransactionPageResponse getTransactions(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) {
        return AccountTransactionPageResponse.from(
                getAccountTransactionsUseCase.getTransactions(
                        new GetAccountTransactionsQuery(
                                new AccountId(accountId),
                                limit,
                                TransactionCursorCodec.decode(cursor))));
    }
}
