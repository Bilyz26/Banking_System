package com.bankingsystem.transfer.application.port.in;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

public record TransferMoneyResult(
        LedgerTransactionId transactionId,
        AccountId sourceAccountId,
        Money sourceBalance,
        LedgerEntryId debitEntryId,
        AccountId destinationAccountId,
        Money destinationBalance,
        LedgerEntryId creditEntryId) {
}
