package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.GetAccountQuery;
import com.bankingsystem.account.application.port.in.GetAccountResult;
import com.bankingsystem.account.application.port.in.GetAccountUseCase;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.BankAccount;

import java.util.Objects;

public final class GetAccountService implements GetAccountUseCase {

    private final AccountRepository accountRepository;

    public GetAccountService(AccountRepository accountRepository) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
    }

    @Override
    public GetAccountResult getAccount(GetAccountQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(query.accountId(), "account id must not be null");

        BankAccount account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));

        return new GetAccountResult(
                account.id(),
                account.ownerId(),
                account.balance(),
                account.status());
    }
}

