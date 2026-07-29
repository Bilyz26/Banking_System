package com.bankingsystem.transfer.presentation;

import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.ledger.domain.LedgerTransactionId;
import com.bankingsystem.shared.domain.Money;
import com.bankingsystem.transfer.application.port.in.TransferMoneyCommand;
import com.bankingsystem.transfer.application.port.in.TransferMoneyUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/transfers")
public final class TransferController {

    private final TransferMoneyUseCase transferMoneyUseCase;

    public TransferController(TransferMoneyUseCase transferMoneyUseCase) {
        this.transferMoneyUseCase = Objects.requireNonNull(
                transferMoneyUseCase,
                "transfer money use case must not be null");
    }

    @PostMapping
    public TransferMoneyResponse transfer(
            @RequestHeader("Idempotency-Key") java.util.UUID idempotencyKey,
            @Valid @RequestBody TransferMoneyRequest request) {
        return TransferMoneyResponse.from(transferMoneyUseCase.transfer(
                new TransferMoneyCommand(
                        new AccountId(request.sourceAccountId()),
                        new AccountId(request.destinationAccountId()),
                        new Money(
                                request.amount(),
                                Currency.getInstance(request.currencyCode())),
                        request.description(),
                        new LedgerTransactionId(idempotencyKey))));
    }
}
