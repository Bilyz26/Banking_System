package com.bankingsystem.transfer.infrastructure;

import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.TransferLedgerEntries;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

public final class JdbcTransferCommitter implements TransferCommitter {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionTemplate transactionTemplate;

    public JdbcTransferCommitter(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository,
            TransactionTemplate transactionTemplate) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.ledgerRepository =
                Objects.requireNonNull(ledgerRepository, "ledger repository must not be null");
        this.transactionTemplate =
                Objects.requireNonNull(transactionTemplate, "transaction template must not be null");
    }

    @Override
    public void commit(
            BankAccount sourceAccount,
            BankAccount destinationAccount,
            TransferLedgerEntries ledgerEntries) {
        transactionTemplate.executeWithoutResult(status -> {
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            ledgerRepository.appendAll(
                    List.of(ledgerEntries.debitEntry(), ledgerEntries.creditEntry()));
        });
    }
}
