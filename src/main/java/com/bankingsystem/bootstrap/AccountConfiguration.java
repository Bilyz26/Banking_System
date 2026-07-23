package com.bankingsystem.bootstrap;

import com.bankingsystem.account.application.AccountLifecycleService;
import com.bankingsystem.account.application.GetAccountService;
import com.bankingsystem.account.application.OpenAccountService;
import com.bankingsystem.account.application.port.in.GetAccountUseCase;
import com.bankingsystem.account.application.port.in.OpenAccountUseCase;
import com.bankingsystem.account.application.port.out.AccountIdGenerator;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.application.port.out.CustomerExistenceChecker;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfiguration {

    @Bean
    AccountRepository accountRepository() {
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
}
