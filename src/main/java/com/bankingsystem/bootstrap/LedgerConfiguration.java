package com.bankingsystem.bootstrap;

import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.ledger.application.GetAccountTransactionsService;
import com.bankingsystem.ledger.application.port.in.GetAccountTransactionsUseCase;
import com.bankingsystem.ledger.application.port.out.LedgerEntryIdGenerator;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import com.bankingsystem.ledger.domain.LedgerEntryId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.ledger.infrastructure.InMemoryLedgerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class LedgerConfiguration {

    @Bean
    @Profile("!postgresql")
    LedgerRepository ledgerRepository() {
        return new InMemoryLedgerRepository();
    }

    @Bean
    LedgerEntryIdGenerator ledgerEntryIdGenerator() {
        return () -> new LedgerEntryId(UUID.randomUUID());
    }

    @Bean
    LedgerTransactionIdGenerator ledgerTransactionIdGenerator() {
        return () -> new LedgerTransactionId(UUID.randomUUID());
    }

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    GetAccountTransactionsUseCase getAccountTransactionsUseCase(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        return new GetAccountTransactionsService(accountRepository, ledgerRepository);
    }
}
