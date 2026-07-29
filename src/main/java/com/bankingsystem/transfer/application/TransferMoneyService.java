package com.bankingsystem.transfer.application;

import com.bankingsystem.account.application.AccountNotFoundException;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.ledger.application.port.out.LedgerEntryIdGenerator;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerEntry;
import com.bankingsystem.ledger.domain.LedgerEntryType;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.domain.TransferLedgerEntries;
import com.bankingsystem.transfer.application.port.in.TransferMoneyCommand;
import com.bankingsystem.transfer.application.port.in.TransferMoneyResult;
import com.bankingsystem.transfer.application.port.in.TransferMoneyUseCase;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;
import com.bankingsystem.transfer.domain.TransferService;
import com.bankingsystem.shared.application.IdempotencyConflictException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class TransferMoneyService implements TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransferService transferService;
    private final TransferCommitter transferCommitter;
    private final LedgerEntryIdGenerator ledgerEntryIdGenerator;
    private final LedgerTransactionIdGenerator transactionIdGenerator;
    private final LedgerRepository ledgerRepository;
    private final Clock clock;

    public TransferMoneyService(
            AccountRepository accountRepository,
            TransferService transferService,
            TransferCommitter transferCommitter,
            LedgerEntryIdGenerator ledgerEntryIdGenerator,
            LedgerTransactionIdGenerator transactionIdGenerator,
            LedgerRepository ledgerRepository,
            Clock clock) {
        this.accountRepository =
                Objects.requireNonNull(accountRepository, "account repository must not be null");
        this.transferService =
                Objects.requireNonNull(transferService, "transfer service must not be null");
        this.transferCommitter =
                Objects.requireNonNull(transferCommitter, "transfer committer must not be null");
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
    public TransferMoneyResult transfer(TransferMoneyCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        TransferMoneyResult replay = replay(command);
        if (replay != null) {
            return replay;
        }
        BankAccount sourceAccount = findAccount(command.sourceAccountId());
        BankAccount destinationAccount = findAccount(command.destinationAccountId());

        transferService.transfer(sourceAccount, destinationAccount, command.amount());

        LedgerEntryId debitEntryId = ledgerEntryIdGenerator.generate();
        LedgerEntryId creditEntryId = ledgerEntryIdGenerator.generate();
        LedgerTransactionId transactionId = command.idempotencyKey() == null
                ? transactionIdGenerator.generate()
                : command.idempotencyKey();
        Instant occurredAt = clock.instant();
        TransferLedgerEntries ledgerEntries = TransferLedgerEntries.posted(
                debitEntryId,
                creditEntryId,
                transactionId,
                sourceAccount.id(),
                destinationAccount.id(),
                command.amount(),
                sourceAccount.balance(),
                destinationAccount.balance(),
                occurredAt,
                command.description());

        transferCommitter.commit(sourceAccount, destinationAccount, ledgerEntries);

        return new TransferMoneyResult(
                transactionId,
                sourceAccount.id(),
                sourceAccount.balance(),
                debitEntryId,
                destinationAccount.id(),
                destinationAccount.balance(),
                creditEntryId);
    }

    private TransferMoneyResult replay(TransferMoneyCommand command) {
        if (command.idempotencyKey() == null) {
            return null;
        }
        List<LedgerEntry> entries =
                ledgerRepository.findByTransactionId(command.idempotencyKey());
        if (entries.isEmpty()) {
            return null;
        }
        LedgerEntry debit = entryOfType(entries, LedgerEntryType.TRANSFER_DEBIT);
        LedgerEntry credit = entryOfType(entries, LedgerEntryType.TRANSFER_CREDIT);
        String description =
                command.description() == null ? "" : command.description().trim();
        if (entries.size() != 2
                || debit == null
                || credit == null
                || !debit.accountId().equals(command.sourceAccountId())
                || !credit.accountId().equals(command.destinationAccountId())
                || !debit.amount().equals(command.amount())
                || !credit.amount().equals(command.amount())
                || !debit.description().equals(description)
                || !credit.description().equals(description)) {
            throw new IdempotencyConflictException();
        }
        return new TransferMoneyResult(
                command.idempotencyKey(),
                debit.accountId(),
                debit.balanceAfter(),
                debit.id(),
                credit.accountId(),
                credit.balanceAfter(),
                credit.id());
    }

    private static LedgerEntry entryOfType(
            List<LedgerEntry> entries,
            LedgerEntryType type) {
        return entries.stream()
                .filter(entry -> entry.type() == type)
                .findFirst()
                .orElse(null);
    }

    private BankAccount findAccount(AccountId accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
