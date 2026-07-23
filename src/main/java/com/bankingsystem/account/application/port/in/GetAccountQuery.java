package com.bankingsystem.account.application.port.in;

import com.bankingsystem.account.domain.AccountId;

public record GetAccountQuery(AccountId accountId) {
}

