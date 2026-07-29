package com.bankingsystem.account.application.port.in;

public interface GetAccountUseCase {

    GetAccountResult getAccount(GetAccountQuery query);
}

