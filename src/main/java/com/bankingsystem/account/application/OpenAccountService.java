package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.OpenAccountCommand;
import com.bankingsystem.account.application.port.in.OpenAccountResult;
import com.bankingsystem.account.application.port.in.OpenAccountUseCase;
import com.bankingsystem.account.application.port.out.AccountIdGenerator;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.application.port.out.CustomerExistenceChecker;
import com.bankingsystem.account.domain.BankAccount;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Application service coordinating account opening for an existing customer.
 */
public final class OpenAccountService implements OpenAccountUseCase {

    private final CustomerExistenceChecker customerExistenceChecker;
    private final AccountRepository accountRepository;
    private final AccountIdGenerator accountIdGenerator;

    public OpenAccountService(
            CustomerExistenceChecker customerExistenceChecker,
            AccountRepository accountRepository,
            AccountIdGenerator accountIdGenerator) {
        this.customerExistenceChecker = Objects.requireNonNull(
                customerExistenceChecker,
                "customer existence checker must not be null");
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.accountIdGenerator =
                Objects.requireNonNull(accountIdGenerator, "account id generator must not be null");
    }

    @Override
    public OpenAccountResult openAccount(OpenAccountCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.ownerId(), "owner id must not be null");

        if (!customerExistenceChecker.exists(command.ownerId())) {
            throw new AccountOwnerNotFoundException(command.ownerId());
        }

        Currency currency = parseCurrency(command.currencyCode());
        BankAccount account = BankAccount.open(
                accountIdGenerator.generate(),
                command.ownerId(),
                currency);

        accountRepository.save(account);

        return new OpenAccountResult(
                account.id(),
                account.ownerId(),
                account.balance(),
                account.status());
    }

    private static Currency parseCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("currency code must not be blank");
        }
        return Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
    }
}
