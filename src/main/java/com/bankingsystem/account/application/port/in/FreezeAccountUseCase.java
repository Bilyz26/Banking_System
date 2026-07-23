package com.bankingsystem.account.application.port.in;

public interface FreezeAccountUseCase {

    ChangeAccountStatusResult freezeAccount(ChangeAccountStatusCommand command);
}

