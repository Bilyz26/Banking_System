package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.domain.LedgerEntry;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

public final class JdbcAccountOperationCommitter implements AccountOperationCommitter {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionTemplate transactionTemplate;

    public JdbcAccountOperationCommitter(
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
    public void commit(BankAccount account, LedgerEntry ledgerEntry) {
        transactionTemplate.executeWithoutResult(status -> {
            accountRepository.save(account);
            ledgerRepository.append(ledgerEntry);
        });
    }
}
