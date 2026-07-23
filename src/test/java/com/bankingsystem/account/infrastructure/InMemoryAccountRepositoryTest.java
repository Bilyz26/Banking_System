package com.bankingsystem.account.infrastructure;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.domain.BankAccount;
import com.bankingsystem.customer.domain.CustomerId;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAccountRepositoryTest {

    @Test
    void savesAndFindsAccountByIdentity() {
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        AccountId accountId =
                new AccountId(UUID.fromString("4261d99d-9ba9-45e0-b55a-c7250f305e05"));
        BankAccount account = BankAccount.open(
                accountId,
                new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d")),
                Currency.getInstance("USD"));

        repository.save(account);

        assertEquals(account, repository.findById(accountId).orElseThrow());
        assertTrue(repository.findById(AccountId.generate()).isEmpty());
    }
}
