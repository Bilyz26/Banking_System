package com.bankingsystem.account.presentation;

import com.bankingsystem.account.application.port.in.MoneyOperationResult;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyOperationResponse(
        UUID accountId,
        UUID ledgerEntryId,
        UUID transactionId,
        BigDecimal balance,
        String currencyCode) {

    static MoneyOperationResponse from(MoneyOperationResult result) {
        return new MoneyOperationResponse(
                result.accountId().value(),
                result.ledgerEntryId().value(),
                result.transactionId().value(),
                result.balance().amount(),
                result.balance().currency().getCurrencyCode());
    }
}
