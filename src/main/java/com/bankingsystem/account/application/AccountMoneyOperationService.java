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
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;
import com.bankingsystem.shared.application.IdempotencyConflictException;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

public final class AccountMoneyOperationService
        implements DepositMoneyUseCase, WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final AccountOperationCommitter accountOperationCommitter;
    private final LedgerEntryIdGenerator ledgerEntryIdGenerator;
    private final LedgerTransactionIdGenerator transactionIdGenerator;
    private final LedgerRepository ledgerRepository;
    private final Clock clock;

    public AccountMoneyOperationService(
            AccountRepository accountRepository,
            AccountOperationCommitter accountOperationCommitter,
            LedgerEntryIdGenerator ledgerEntryIdGenerator,
            LedgerTransactionIdGenerator transactionIdGenerator,
            LedgerRepository ledgerRepository,
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
        this.ledgerRepository =
                Objects.requireNonNull(ledgerRepository, "ledger repository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public MoneyOperationResult deposit(DepositMoneyCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        MoneyOperationResult replay = replay(
                command.idempotencyKey(),
                command.accountId(),
                LedgerEntryType.DEPOSIT,
                command.amount(),
                command.description());
        if (replay != null) {
            return replay;
        }
        BankAccount account = findAccount(command.accountId());

        account.deposit(command.amount());
        return commit(
                account,
                LedgerEntryType.DEPOSIT,
                command.amount(),
                command.description(),
                command.idempotencyKey());
    }

    @Override
    public MoneyOperationResult withdraw(WithdrawMoneyCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        MoneyOperationResult replay = replay(
                command.idempotencyKey(),
                command.accountId(),
                LedgerEntryType.WITHDRAWAL,
                command.amount(),
                command.description());
        if (replay != null) {
            return replay;
        }
        BankAccount account = findAccount(command.accountId());

        account.withdraw(command.amount());
        return commit(
                account,
                LedgerEntryType.WITHDRAWAL,
                command.amount(),
                command.description(),
                command.idempotencyKey());
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
            String description,
            LedgerTransactionId idempotencyKey) {
        LedgerEntryId ledgerEntryId = ledgerEntryIdGenerator.generate();
        LedgerTransactionId transactionId =
                idempotencyKey == null ? transactionIdGenerator.generate() : idempotencyKey;
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

    private MoneyOperationResult replay(
            LedgerTransactionId idempotencyKey,
            AccountId accountId,
            LedgerEntryType entryType,
            Money amount,
            String description) {
        if (idempotencyKey == null) {
            return null;
        }
        List<LedgerEntry> entries = ledgerRepository.findByTransactionId(idempotencyKey);
        if (entries.isEmpty()) {
            return null;
        }
        if (entries.size() != 1) {
            throw new IdempotencyConflictException();
        }
        LedgerEntry entry = entries.getFirst();
        String normalizedDescription = description == null ? "" : description.trim();
        if (!entry.accountId().equals(accountId)
                || entry.type() != entryType
                || !entry.amount().equals(amount)
                || !entry.description().equals(normalizedDescription)) {
            throw new IdempotencyConflictException();
        }
        return new MoneyOperationResult(
                entry.accountId(),
                entry.id(),
                entry.transactionId(),
                entry.balanceAfter());
    }
}
