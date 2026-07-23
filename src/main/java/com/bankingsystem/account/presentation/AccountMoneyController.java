package com.bankingsystem.account.presentation;

import com.bankingsystem.account.application.port.in.DepositMoneyCommand;
import com.bankingsystem.account.application.port.in.DepositMoneyUseCase;
import com.bankingsystem.account.application.port.in.WithdrawMoneyCommand;
import com.bankingsystem.account.application.port.in.WithdrawMoneyUseCase;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.Money;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
public final class AccountMoneyController {

    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

    public AccountMoneyController(
            DepositMoneyUseCase depositMoneyUseCase,
            WithdrawMoneyUseCase withdrawMoneyUseCase) {
        this.depositMoneyUseCase = Objects.requireNonNull(
                depositMoneyUseCase,
                "deposit money use case must not be null");
        this.withdrawMoneyUseCase = Objects.requireNonNull(
                withdrawMoneyUseCase,
                "withdraw money use case must not be null");
    }

    @PostMapping("/deposits")
    public MoneyOperationResponse deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyOperationRequest request) {
        return MoneyOperationResponse.from(depositMoneyUseCase.deposit(
                new DepositMoneyCommand(
                        new AccountId(accountId),
                        money(request),
                        request.description())));
    }

    @PostMapping("/withdrawals")
    public MoneyOperationResponse withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyOperationRequest request) {
        return MoneyOperationResponse.from(withdrawMoneyUseCase.withdraw(
                new WithdrawMoneyCommand(
                        new AccountId(accountId),
                        money(request),
                        request.description())));
    }

    private static Money money(MoneyOperationRequest request) {
        return new Money(
                request.amount(),
                java.util.Currency.getInstance(request.currencyCode()));
    }
}
