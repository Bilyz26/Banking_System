package com.bankingsystem.customer.presentation;

import com.bankingsystem.customer.application.port.in.CreateCustomerCommand;
import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.application.port.in.CreateCustomerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/customers")
public final class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase =
                Objects.requireNonNull(createCustomerUseCase, "create customer use case must not be null");
    }

    @PostMapping
    public ResponseEntity<CreateCustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CreateCustomerResult result = createCustomerUseCase.createCustomer(
                new CreateCustomerCommand(request.fullName(), request.emailAddress()));

        CreateCustomerResponse response = new CreateCustomerResponse(
                result.customerId().value(),
                result.fullName(),
                result.emailAddress());
        URI location = URI.create("/api/v1/customers/" + result.customerId().value());

        return ResponseEntity.created(location).body(response);
    }
}

