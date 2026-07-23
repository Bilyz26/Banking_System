package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;

import java.util.Objects;

/**
 * Serializes temporary in-memory account and ledger commits.
 */
public final class InMemoryAccountOperationCommitter implements AccountOperationCommitter {

    private final InMemoryAccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public InMemoryAccountOperationCommitter(
            InMemoryAccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.ledgerRepository =
                Objects.requireNonNull(ledgerRepository, "ledger repository must not be null");
    }

    @Override
    public synchronized void commit(BankAccount account, LedgerEntry ledgerEntry) {
        ledgerRepository.append(ledgerEntry);
        accountRepository.save(account);
    }
}
