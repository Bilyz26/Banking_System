package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.Money;

public record OpenAccountResult(
        AccountId accountId,
        CustomerId ownerId,
        Money balance,
        AccountStatus status) {
}

