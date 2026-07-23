package com.bankingsystem.account.application.port.out;

import com.bankingsystem.account.domain.AccountId;

@FunctionalInterface
public interface AccountIdGenerator {

    AccountId generate();
}

