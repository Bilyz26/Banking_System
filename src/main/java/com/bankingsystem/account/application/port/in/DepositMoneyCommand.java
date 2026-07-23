package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.shared.domain.Money;

public record DepositMoneyCommand(
        AccountId accountId,
        Money amount,
        String description) {
}
