package com.bankingsystem.customer.presentation;

import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.domain.CustomerId;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerControllerTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString("a6386d2c-2a8c-43fa-a2cc-a4dba92ee50e");

    @Test
    void returnsCreatedCustomerAndLocation() {
        CustomerController controller = new CustomerController(command ->
                new CreateCustomerResult(
                        new CustomerId(CUSTOMER_ID),
                        command.fullName().trim(),
                        command.emailAddress().trim().toLowerCase()));

        ResponseEntity<CreateCustomerResponse> response = controller.createCustomer(
                new CreateCustomerRequest("Ada Lovelace", "ada@example.com"));

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/api/v1/customers/" + CUSTOMER_ID, response.getHeaders().getLocation().toString());
        assertEquals(CUSTOMER_ID, response.getBody().customerId());
    }
}
