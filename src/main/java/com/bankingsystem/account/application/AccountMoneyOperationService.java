package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.DepositMoneyCommand;
import com.bankingsystem.account.application.port.in.DepositMoneyUseCase;
import com.bankingsystem.account.application.port.in.MoneyOperationResult;
import com.bankingsystem.account.application.port.in.WithdrawMoneyCommand;
import com.bankingsystem.account.application.port.in.WithdrawMoneyUseCase;
import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.application.port.out.LedgerEntryIdGenerator;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;

import java.time.Clock;
import java.util.Objects;

public final class AccountMoneyOperationService
        implements DepositMoneyUseCase, WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final AccountOperationCommitter accountOperationCommitter;
    private final LedgerEntryIdGenerator ledgerEntryIdGenerator;
    private final LedgerTransactionIdGenerator transactionIdGenerator;
    private final Clock clock;

    public AccountMoneyOperationService(
            AccountRepository accountRepository,
            AccountOperationCommitter accountOperationCommitter,
            LedgerEntryIdGenerator ledgerEntryIdGenerator,
            LedgerTransactionIdGenerator transactionIdGenerator,
            Clock clock) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.accountOperationCommitter = Objects.requireNonNull(
                accountOperationCommitter,
                "account operation committer must not be null");
        this.ledgerEntryIdGenerator = Objects.requireNonNull(
                ledgerEntryIdGenerator,
                "ledger entry id generator must not be null");
        this.transactionIdGenerator = Objects.requireNonNull(
                transactionIdGenerator,
                "ledger transaction id generator must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public MoneyOperationResult deposit(DepositMoneyCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        BankAccount account = findAccount(command.accountId());

        account.deposit(command.amount());
        return commit(
                account,
                LedgerEntryType.DEPOSIT,
                command.amount(),
                command.description());
    }

    @Override
    public MoneyOperationResult withdraw(WithdrawMoneyCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        BankAccount account = findAccount(command.accountId());

        account.withdraw(command.amount());
        return commit(
                account,
                LedgerEntryType.WITHDRAWAL,
                command.amount(),
                command.description());
    }

    private BankAccount findAccount(AccountId accountId) {
        Objects.requireNonNull(accountId, "account id must not be null");
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private MoneyOperationResult commit(
            BankAccount account,
            LedgerEntryType entryType,
            Money amount,
            String description) {
        LedgerEntryId ledgerEntryId = ledgerEntryIdGenerator.generate();
        LedgerTransactionId transactionId = transactionIdGenerator.generate();
        LedgerEntry ledgerEntry = new LedgerEntry(
                ledgerEntryId,
                transactionId,
                account.id(),
                entryType,
                amount,
                account.balance(),
                clock.instant(),
                description);

        accountOperationCommitter.commit(account, ledgerEntry);

        return new MoneyOperationResult(
                account.id(),
                ledgerEntryId,
                transactionId,
                account.balance());
    }
}
