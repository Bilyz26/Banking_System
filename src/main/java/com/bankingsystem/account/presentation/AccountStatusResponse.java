package com.bankingsystem.account.presentation;

import com.bankingsystem.account.application.port.in.ChangeAccountStatusResult;
import com.bankingsystem.account.domain.AccountStatus;

import java.util.UUID;

public record AccountStatusResponse(
        UUID accountId,
        AccountStatus status) {

    static AccountStatusResponse from(ChangeAccountStatusResult result) {
        return new AccountStatusResponse(result.accountId().value(), result.status());
    }
}
