package com.bankingsystem.customer.presentation;

import com.bankingsystem.customer.application.port.in.CreateCustomerCommand;
import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.application.port.in.CreateCustomerUseCase;
import com.bankingsystem.customer.application.port.in.GetCustomerQuery;
import com.bankingsystem.customer.application.port.in.GetCustomerUseCase;
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
@RequestMapping("/api/v1/customers")
public final class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;

    public CustomerController(
            CreateCustomerUseCase createCustomerUseCase,
            GetCustomerUseCase getCustomerUseCase) {
        this.createCustomerUseCase =
                Objects.requireNonNull(createCustomerUseCase, "create customer use case must not be null");
        this.getCustomerUseCase =
                Objects.requireNonNull(getCustomerUseCase, "get customer use case must not be null");
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

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable UUID customerId) {
        return CustomerResponse.from(getCustomerUseCase.getCustomer(
                new GetCustomerQuery(new CustomerId(customerId))));
    }
}
