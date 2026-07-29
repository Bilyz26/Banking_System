package com.bankingsystem.account.presentation;

import com.bankingsystem.account.application.port.in.ChangeAccountStatusCommand;
import com.bankingsystem.account.application.port.in.CloseAccountUseCase;
import com.bankingsystem.account.application.port.in.FreezeAccountUseCase;
import com.bankingsystem.account.application.port.in.GetAccountQuery;
import com.bankingsystem.account.application.port.in.GetAccountUseCase;
import com.bankingsystem.account.application.port.in.OpenAccountCommand;
import com.bankingsystem.account.application.port.in.OpenAccountResult;
import com.bankingsystem.account.application.port.in.OpenAccountUseCase;
import com.bankingsystem.account.application.port.in.UnfreezeAccountUseCase;
import com.bankingsystem.account.domain.AccountId;
import com.bankingsystem.customer.domain.CustomerId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public final class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final FreezeAccountUseCase freezeAccountUseCase;
    private final UnfreezeAccountUseCase unfreezeAccountUseCase;
    private final CloseAccountUseCase closeAccountUseCase;

    public AccountController(
            OpenAccountUseCase openAccountUseCase,
            GetAccountUseCase getAccountUseCase,
            FreezeAccountUseCase freezeAccountUseCase,
            UnfreezeAccountUseCase unfreezeAccountUseCase,
            CloseAccountUseCase closeAccountUseCase) {
        this.openAccountUseCase = Objects.requireNonNull(
                openAccountUseCase,
                "open account use case must not be null");
        this.getAccountUseCase =
                Objects.requireNonNull(getAccountUseCase, "get account use case must not be null");
        this.freezeAccountUseCase = Objects.requireNonNull(
                freezeAccountUseCase,
                "freeze account use case must not be null");
        this.unfreezeAccountUseCase = Objects.requireNonNull(
                unfreezeAccountUseCase,
                "unfreeze account use case must not be null");
        this.closeAccountUseCase = Objects.requireNonNull(
                closeAccountUseCase,
                "close account use case must not be null");
    }

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(
            @Valid @RequestBody OpenAccountRequest request) {
        OpenAccountResult result = openAccountUseCase.openAccount(new OpenAccountCommand(
                new CustomerId(request.ownerId()),
                request.currencyCode()));
        URI location = URI.create("/api/v1/accounts/" + result.accountId().value());
        return ResponseEntity.created(location).body(AccountResponse.from(result));
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return AccountResponse.from(
                getAccountUseCase.getAccount(new GetAccountQuery(new AccountId(accountId))));
    }

    @PostMapping("/{accountId}/freeze")
    public AccountStatusResponse freezeAccount(@PathVariable UUID accountId) {
        return AccountStatusResponse.from(freezeAccountUseCase.freezeAccount(
                statusCommand(accountId)));
    }

    @PostMapping("/{accountId}/unfreeze")
    public AccountStatusResponse unfreezeAccount(@PathVariable UUID accountId) {
        return AccountStatusResponse.from(unfreezeAccountUseCase.unfreezeAccount(
                statusCommand(accountId)));
    }

    @PostMapping("/{accountId}/close")
    public AccountStatusResponse closeAccount(@PathVariable UUID accountId) {
        return AccountStatusResponse.from(closeAccountUseCase.closeAccount(
                statusCommand(accountId)));
    }

    private static ChangeAccountStatusCommand statusCommand(UUID accountId) {
        return new ChangeAccountStatusCommand(new AccountId(accountId));
    }
}
