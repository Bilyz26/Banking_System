package com.bankingsystem.account.application.port.in;

public interface CloseAccountUseCase {

    ChangeAccountStatusResult closeAccount(ChangeAccountStatusCommand command);
}

