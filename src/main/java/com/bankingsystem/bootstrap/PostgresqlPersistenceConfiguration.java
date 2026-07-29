package com.bankingsystem.bootstrap;

import com.bankingsystem.account.application.port.out.AccountOperationCommitter;
import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.infrastructure.JdbcAccountOperationCommitter;
import com.bankingsystem.account.infrastructure.JdbcAccountRepository;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.infrastructure.JdbcCustomerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.infrastructure.JdbcLedgerRepository;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;
import com.bankingsystem.transfer.infrastructure.JdbcTransferCommitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("postgresql")
public class PostgresqlPersistenceConfiguration {

    @Bean
    TransactionTemplate bankingTransactionTemplate(
            PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    CustomerRepository customerRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcCustomerRepository(jdbcTemplate);
    }

    @Bean
    AccountRepository accountRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcAccountRepository(jdbcTemplate);
    }

    @Bean
    LedgerRepository ledgerRepository(
            JdbcTemplate jdbcTemplate,
            TransactionTemplate bankingTransactionTemplate) {
        return new JdbcLedgerRepository(jdbcTemplate, bankingTransactionTemplate);
    }

    @Bean
    AccountOperationCommitter accountOperationCommitter(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository,
            TransactionTemplate bankingTransactionTemplate) {
        return new JdbcAccountOperationCommitter(
                accountRepository,
                ledgerRepository,
                bankingTransactionTemplate);
    }

    @Bean
    TransferCommitter transferCommitter(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository,
            TransactionTemplate bankingTransactionTemplate) {
        return new JdbcTransferCommitter(
                accountRepository,
                ledgerRepository,
                bankingTransactionTemplate);
    }
}
