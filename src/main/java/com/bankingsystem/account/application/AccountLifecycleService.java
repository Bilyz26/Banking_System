package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.ChangeAccountStatusCommand;
import com.bankingsystem.account.application.port.in.ChangeAccountStatusResult;
import com.bankingsystem.account.application.port.in.CloseAccountUseCase;
import com.bankingsystem.account.application.port.in.FreezeAccountUseCase;
import com.bankingsystem.account.application.port.in.UnfreezeAccountUseCase;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.BankAccount;

import java.util.Objects;
import java.util.function.Consumer;

public final class AccountLifecycleService
        implements FreezeAccountUseCase, UnfreezeAccountUseCase, CloseAccountUseCase {

    private final AccountRepository accountRepository;

    public AccountLifecycleService(AccountRepository accountRepository) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
    }

    @Override
    public ChangeAccountStatusResult freezeAccount(ChangeAccountStatusCommand command) {
        return changeStatus(command, BankAccount::freeze);
    }

    @Override
    public ChangeAccountStatusResult unfreezeAccount(ChangeAccountStatusCommand command) {
        return changeStatus(command, BankAccount::unfreeze);
    }

    @Override
    public ChangeAccountStatusResult closeAccount(ChangeAccountStatusCommand command) {
        return changeStatus(command, BankAccount::close);
    }

    private ChangeAccountStatusResult changeStatus(
            ChangeAccountStatusCommand command,
            Consumer<BankAccount> statusChange) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.accountId(), "account id must not be null");

        BankAccount account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        statusChange.accept(account);
        accountRepository.save(account);

        return new ChangeAccountStatusResult(account.id(), account.status());
    }
}

