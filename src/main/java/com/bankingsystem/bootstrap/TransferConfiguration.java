package com.bankingsystem.bootstrap;

import com.bankingsystem.account.application.port.out.AccountRepository;
import com.bankingsystem.account.infrastructure.InMemoryAccountRepository;
import com.bankingsystem.ledger.application.port.out.LedgerEntryIdGenerator;
import com.bankingsystem.ledger.application.port.out.LedgerRepository;
import com.bankingsystem.ledger.application.port.out.LedgerTransactionIdGenerator;
import com.bankingsystem.transfer.application.TransferMoneyService;
import com.bankingsystem.transfer.application.port.in.TransferMoneyUseCase;
import com.bankingsystem.transfer.application.port.out.TransferCommitter;
import com.bankingsystem.transfer.domain.TransferService;
import com.bankingsystem.transfer.infrastructure.InMemoryTransferCommitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;

@Configuration
public class TransferConfiguration {

    @Bean
    TransferService transferService() {
        return new TransferService();
    }

    @Bean
    @Profile("!postgresql")
    TransferCommitter transferCommitter(
            InMemoryAccountRepository accountRepository,
            LedgerRepository ledgerRepository) {
        return new InMemoryTransferCommitter(accountRepository, ledgerRepository);
    }

    @Bean
    TransferMoneyUseCase transferMoneyUseCase(
            AccountRepository accountRepository,
            TransferService transferService,
            TransferCommitter transferCommitter,
            LedgerEntryIdGenerator ledgerEntryIdGenerator,
            LedgerTransactionIdGenerator transactionIdGenerator,
            Clock applicationClock) {
        return new TransferMoneyService(
                accountRepository,
                transferService,
                transferCommitter,
                ledgerEntryIdGenerator,
                transactionIdGenerator,
                applicationClock);
    }
}
