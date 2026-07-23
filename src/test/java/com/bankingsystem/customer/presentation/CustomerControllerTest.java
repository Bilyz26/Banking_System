package com.bankingsystem.customer.presentation;

import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.application.port.in.GetCustomerResult;
import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileResult;
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
        CustomerController controller = controller();

        ResponseEntity<CreateCustomerResponse> response = controller.createCustomer(
                new CreateCustomerRequest("Ada Lovelace", "ada@example.com"));

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/api/v1/customers/" + CUSTOMER_ID, response.getHeaders().getLocation().toString());
        assertEquals(CUSTOMER_ID, response.getBody().customerId());
    }

    @Test
    void returnsCustomer() {
        CustomerResponse response = controller().getCustomer(CUSTOMER_ID);

        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals("Ada Lovelace", response.fullName());
        assertEquals("ada@example.com", response.emailAddress());
    }

    @Test
    void returnsUpdatedCustomer() {
        CustomerResponse response = controller().updateCustomerProfile(
                CUSTOMER_ID,
                new UpdateCustomerProfileRequest(
                        "Augusta Ada King",
                        "ada.king@example.com"));

        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals("Augusta Ada King", response.fullName());
        assertEquals("ada.king@example.com", response.emailAddress());
    }

    private static CustomerController controller() {
        return new CustomerController(
                command -> new CreateCustomerResult(
                        new CustomerId(CUSTOMER_ID),
                        command.fullName().trim(),
                        command.emailAddress().trim().toLowerCase()),
                query -> new GetCustomerResult(
                        query.customerId(),
                        "Ada Lovelace",
                        "ada@example.com"),
                command -> new UpdateCustomerProfileResult(
                        command.customerId(),
                        command.fullName().trim(),
                        command.emailAddress().trim().toLowerCase()));
    }
}
