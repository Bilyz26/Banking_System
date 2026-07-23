package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.OpenAccountCommand;
import com.bankingsystem.account.application.port.in.OpenAccountResult;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.application.port.out.CustomerExistenceChecker;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAccountServiceTest {

    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("c0347db4-3f49-45a7-b4be-516dfda772ed"));
    private static final AccountId ACCOUNT_ID =
            new AccountId(UUID.fromString("975a7af9-c555-4f16-9d36-28b9d0aee96b"));

    private final InMemoryCustomerExistenceChecker customerExistenceChecker =
            new InMemoryCustomerExistenceChecker();
    private final InMemoryAccountRepository accountRepository =
            new InMemoryAccountRepository();
    private final OpenAccountService service = new OpenAccountService(
            customerExistenceChecker,
            accountRepository,
            () -> ACCOUNT_ID);

    @Test
    void opensAZeroBalanceActiveAccountForAnExistingCustomer() {
        customerExistenceChecker.add(CUSTOMER_ID);

        OpenAccountResult result =
                service.openAccount(new OpenAccountCommand(CUSTOMER_ID, " usd "));

        assertEquals(ACCOUNT_ID, result.accountId());
        assertEquals(CUSTOMER_ID, result.ownerId());
        assertEquals(Money.of("0.00", "USD"), result.balance());
        assertEquals(AccountStatus.ACTIVE, result.status());
        assertEquals(1, accountRepository.accounts.size());
    }

    @Test
    void rejectsAnAccountForAnUnknownCustomer() {
        assertThrows(
                AccountOwnerNotFoundException.class,
                () -> service.openAccount(new OpenAccountCommand(CUSTOMER_ID, "USD")));
        assertEquals(0, accountRepository.accounts.size());
    }

    @Test
    void rejectsAnInvalidCurrencyCode() {
        customerExistenceChecker.add(CUSTOMER_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.openAccount(new OpenAccountCommand(CUSTOMER_ID, "INVALID")));
        assertEquals(0, accountRepository.accounts.size());
    }

    private static final class InMemoryCustomerExistenceChecker
            implements CustomerExistenceChecker {

        private final Map<CustomerId, Boolean> existingCustomers = new HashMap<>();

        @Override
        public boolean exists(CustomerId customerId) {
            return existingCustomers.containsKey(customerId);
        }

        private void add(CustomerId customerId) {
            existingCustomers.put(customerId, true);
        }
    }

    private static final class InMemoryAccountRepository implements AccountRepository {

        private final Map<AccountId, BankAccount> accounts = new HashMap<>();

        @Override
        public Optional<BankAccount> findById(AccountId accountId) {
            return Optional.ofNullable(accounts.get(accountId));
        }

        @Override
        public void save(BankAccount account) {
            accounts.put(account.id(), account);
        }
    }
}
