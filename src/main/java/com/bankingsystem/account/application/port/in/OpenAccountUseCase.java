package com.bankingsystem.account.application.port.in;

public interface OpenAccountUseCase {

    OpenAccountResult openAccount(OpenAccountCommand command);
}

