package com.bankingsystem.account.application.port.in;

public interface DepositMoneyUseCase {

    MoneyOperationResult deposit(DepositMoneyCommand command);
}
