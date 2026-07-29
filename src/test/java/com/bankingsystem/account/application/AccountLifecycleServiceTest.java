package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.ChangeAccountStatusCommand;
import com.bankingsystem.account.application.port.in.ChangeAccountStatusResult;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.account.domain.NonZeroBalanceException;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountLifecycleServiceTest {

    private static final AccountId ACCOUNT_ID =
            new AccountId(UUID.fromString("4261d99d-9ba9-45e0-b55a-c7250f305e05"));
    private static final CustomerId OWNER_ID =
            new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d"));

    private final TestAccountRepository repository = new TestAccountRepository();
    private final AccountLifecycleService service =
            new AccountLifecycleService(repository);

    @Test
    void freezesAndUnfreezesAccount() {
        repository.save(openAccount());

        ChangeAccountStatusResult frozen =
                service.freezeAccount(new ChangeAccountStatusCommand(ACCOUNT_ID));
        ChangeAccountStatusResult active =
                service.unfreezeAccount(new ChangeAccountStatusCommand(ACCOUNT_ID));

        assertEquals(AccountStatus.FROZEN, frozen.status());
        assertEquals(AccountStatus.ACTIVE, active.status());
        assertEquals(3, repository.saveCount);
    }

    @Test
    void closesZeroBalanceAccount() {
        repository.save(openAccount());

        ChangeAccountStatusResult result =
                service.closeAccount(new ChangeAccountStatusCommand(ACCOUNT_ID));

        assertEquals(AccountStatus.CLOSED, result.status());
        assertEquals(AccountStatus.CLOSED, repository.findById(ACCOUNT_ID).orElseThrow().status());
    }

    @Test
    void failedClosureDoesNotSaveAccount() {
        BankAccount account = openAccount();
        account.deposit(Money.of("10.00", "USD"));
        repository.save(account);
        int savesBeforeClosure = repository.saveCount;

        assertThrows(
                NonZeroBalanceException.class,
                () -> service.closeAccount(new ChangeAccountStatusCommand(ACCOUNT_ID)));

        assertEquals(savesBeforeClosure, repository.saveCount);
        assertEquals(AccountStatus.ACTIVE, account.status());
    }

    @Test
    void rejectsUnknownAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> service.freezeAccount(new ChangeAccountStatusCommand(ACCOUNT_ID)));
    }

    private static BankAccount openAccount() {
        return BankAccount.open(
                ACCOUNT_ID,
                OWNER_ID,
                Currency.getInstance("USD"));
    }

    private static final class TestAccountRepository implements AccountRepository {

        private final Map<AccountId, BankAccount> accounts = new HashMap<>();
        private int saveCount;

        @Override
        public Optional<BankAccount> findById(AccountId accountId) {
            return Optional.ofNullable(accounts.get(accountId));
        }

        @Override
        public void save(BankAccount account) {
            accounts.put(account.id(), account);
            saveCount++;
        }
    }
}

