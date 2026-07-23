package com.bankingsystem.account.application.port.in;

public interface WithdrawMoneyUseCase {

    MoneyOperationResult withdraw(WithdrawMoneyCommand command);
}
