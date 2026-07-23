package com.bankingsystem.account.application.port.out;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;

import java.util.Optional;

public interface AccountRepository {

    Optional<BankAccount> findById(AccountId accountId);

    void save(BankAccount account);
}

