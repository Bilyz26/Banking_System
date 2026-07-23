package com.bankingsystem.account.application.port.in;

public interface UnfreezeAccountUseCase {

    ChangeAccountStatusResult unfreezeAccount(ChangeAccountStatusCommand command);
}

