package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;

public record ChangeAccountStatusResult(
        AccountId accountId,
        AccountStatus status) {
}

