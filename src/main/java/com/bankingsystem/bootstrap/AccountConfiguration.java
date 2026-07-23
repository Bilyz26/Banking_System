package com.bankingsystem.bootstrap;

import com.bankingsystem.account.application.AccountLifecycleService;
import com.bankingsystem.account.application.AccountMoneyOperationService;
import com.bankingsystem.account.application.GetAccountService;
import com.bankingsystem.account.application.OpenAccountService;
import com.bankingsystem.account.application.port.in.GetAccountUseCase;
import com.bankingsystem.account.application.port.in.OpenAccountUseCase;
import com.bankingsystem.account.application.port.out.AccountIdGenerator;
import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.application.port.out.CustomerExistenceChecker;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.account.infrastructure.InMemoryAccountOperationCommitter;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerEntryIdGenerator;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;

@Configuration
public class AccountConfiguration {

    @Bean
    @Profile("!postgresql")
    InMemoryAccountRepository accountRepository() {
        return new InMemoryAccountRepository();
    }

    @Bean
    AccountIdGenerator accountIdGenerator() {
        return AccountId::generate;
    }

    @Bean
    CustomerExistenceChecker customerExistenceChecker(
            CustomerRepository customerRepository) {
        return customerId -> customerRepository.findById(customerId).isPresent();
    }

    @Bean
    OpenAccountUseCase openAccountUseCase(
            CustomerExistenceChecker customerExistenceChecker,
            AccountRepository accountRepository,
            AccountIdGenerator accountIdGenerator) {
        return new OpenAccountService(
                customerExistenceChecker,
                accountRepository,
                accountIdGenerator);
    }

    @Bean
    GetAccountUseCase getAccountUseCase(AccountRepository accountRepository) {
        return new GetAccountService(accountRepository);
    }

    @Bean
    AccountLifecycleService accountLifecycleService(
            AccountRepository accountRepository) {
        return new AccountLifecycleService(accountRepository);
    }

    @Bean
    @Profile("!postgresql")
    AccountOperationCommitter accountOperationCommitter(
            InMemoryAccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        return new InMemoryAccountOperationCommitter(accountRepository, ledgerRepository);
    }

    @Bean
    AccountMoneyOperationService accountMoneyOperationService(
            AccountRepository accountRepository,
            AccountOperationCommitter accountOperationCommitter,
            LedgerEntryIdGenerator ledgerEntryIdGenerator,
            LedgerTransactionIdGenerator ledgerTransactionIdGenerator,
            LedgerRepository ledgerRepository,
            Clock applicationClock) {
        return new AccountMoneyOperationService(
                accountRepository,
                accountOperationCommitter,
                ledgerEntryIdGenerator,
                ledgerTransactionIdGenerator,
                ledgerRepository,
                applicationClock);
    }
}
