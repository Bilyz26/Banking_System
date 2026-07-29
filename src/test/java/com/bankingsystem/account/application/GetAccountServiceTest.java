package com.bankingsystem.account.application;

import com.bankingsystem.account.application.port.in.GetAccountQuery;
import com.bankingsystem.account.application.port.in.GetAccountResult;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.AccountStatus;
import com.bankingsystem.account.domain.BankAccount;
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

class GetAccountServiceTest {

    private static final AccountId ACCOUNT_ID =
            new AccountId(UUID.fromString("4261d99d-9ba9-45e0-b55a-c7250f305e05"));
    private static final CustomerId OWNER_ID =
            new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d"));

    private final TestAccountRepository repository = new TestAccountRepository();
    private final GetAccountService service = new GetAccountService(repository);

    @Test
    void returnsAccountDetails() {
        BankAccount account =
                BankAccount.open(ACCOUNT_ID, OWNER_ID, Currency.getInstance("USD"));
        repository.save(account);

        GetAccountResult result = service.getAccount(new GetAccountQuery(ACCOUNT_ID));

        assertEquals(ACCOUNT_ID, result.accountId());
        assertEquals(OWNER_ID, result.ownerId());
        assertEquals(Money.of("0.00", "USD"), result.balance());
        assertEquals(AccountStatus.ACTIVE, result.status());
    }

    @Test
    void rejectsUnknownAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> service.getAccount(new GetAccountQuery(ACCOUNT_ID)));
    }

    private static final class TestAccountRepository implements AccountRepository {

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

