package com.bankingsystem.transfer.infrastructure;

import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.TransferLedgerEntries;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;

import java.util.List;
import java.util.Objects;

/**
 * Temporary in-memory transfer commit adapter.
 */
public final class InMemoryTransferCommitter implements TransferCommitter {

    private final InMemoryAccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public InMemoryTransferCommitter(
            InMemoryAccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.ledgerRepository =
                Objects.requireNonNull(ledgerRepository, "ledger repository must not be null");
    }

    @Override
    public synchronized void commit(
            BankAccount sourceAccount,
            BankAccount destinationAccount,
            TransferLedgerEntries ledgerEntries) {
        Objects.requireNonNull(sourceAccount, "source account must not be null");
        Objects.requireNonNull(destinationAccount, "destination account must not be null");
        Objects.requireNonNull(ledgerEntries, "transfer ledger entries must not be null");

        ledgerRepository.appendAll(
                List.of(ledgerEntries.debitEntry(), ledgerEntries.creditEntry()));
        accountRepository.saveAll(sourceAccount, destinationAccount);
    }
}
