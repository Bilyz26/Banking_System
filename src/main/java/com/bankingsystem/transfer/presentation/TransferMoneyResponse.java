package com.bankingsystem.transfer.presentation;

import com.bankingsystem.transfer.application.port.in.TransferMoneyResult;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyResponse(
        UUID transactionId,
        UUID sourceAccountId,
        BigDecimal sourceBalance,
        UUID debitEntryId,
        UUID destinationAccountId,
        BigDecimal destinationBalance,
        UUID creditEntryId,
        String currencyCode) {

    static TransferMoneyResponse from(TransferMoneyResult result) {
        return new TransferMoneyResponse(
                result.transactionId().value(),
                result.sourceAccountId().value(),
                result.sourceBalance().amount(),
                result.debitEntryId().value(),
                result.destinationAccountId().value(),
                result.destinationBalance().amount(),
                result.creditEntryId().value(),
                result.sourceBalance().currency().getCurrencyCode());
    }
}
