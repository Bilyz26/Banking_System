package com.bankingsystem.account.presentation;

import com.bankingsystem.account.application.port.in.GetAccountResult;
import com.bankingsystem.account.application.port.in.OpenAccountResult;
import com.bankingsystem.account.domain.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        UUID ownerId,
        BigDecimal balance,
        String currencyCode,
        AccountStatus status) {

    static AccountResponse from(OpenAccountResult result) {
        return new AccountResponse(
                result.accountId().value(),
                result.ownerId().value(),
                result.balance().amount(),
                result.balance().currency().getCurrencyCode(),
                result.status());
    }

    static AccountResponse from(GetAccountResult result) {
        return new AccountResponse(
                result.accountId().value(),
                result.ownerId().value(),
                result.balance().amount(),
                result.balance().currency().getCurrencyCode(),
                result.status());
    }
}
