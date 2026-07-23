package com.bankingsystem.customer.infrastructure;

import com.bankingsystem.customer.application.DuplicateCustomerEmailException;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCustomerRepositoryTest {

    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d"));
    private static final CustomerId OTHER_CUSTOMER_ID =
            new CustomerId(UUID.fromString("97e3e440-e76a-481a-9ce8-918f46ad64e1"));

    private final InMemoryCustomerRepository repository =
            new InMemoryCustomerRepository();

    @Test
    void savesAndFindsCustomerByIdentity() {
        Customer customer = new Customer(
                CUSTOMER_ID,
                "Ada Lovelace",
                "ada@example.com");

        repository.save(customer);

        assertEquals(customer, repository.findById(CUSTOMER_ID).orElseThrow());
        assertTrue(repository.existsByEmailAddress("ada@example.com"));
    }

    @Test
    void updatingEmailReleasesThePreviousEmailIndex() {
        repository.save(new Customer(CUSTOMER_ID, "Ada", "ada@example.com"));

        repository.save(
                new Customer(CUSTOMER_ID, "Ada Lovelace", "new.ada@example.com"));

        assertFalse(repository.existsByEmailAddress("ada@example.com"));
        assertTrue(repository.existsByEmailAddress("new.ada@example.com"));
    }

    @Test
    void duplicateEmailDoesNotCorruptStoredCustomers() {
        Customer ada = new Customer(CUSTOMER_ID, "Ada", "ada@example.com");
        Customer grace = new Customer(
                OTHER_CUSTOMER_ID,
                "Grace",
                "grace@example.com");
        repository.save(ada);
        repository.save(grace);

        assertThrows(
                DuplicateCustomerEmailException.class,
                () -> repository.save(
                        new Customer(CUSTOMER_ID, "Ada Lovelace", "grace@example.com")));

        assertEquals(ada, repository.findById(CUSTOMER_ID).orElseThrow());
        assertEquals(grace, repository.findById(OTHER_CUSTOMER_ID).orElseThrow());
        assertTrue(repository.existsByEmailAddress("ada@example.com"));
        assertTrue(repository.existsByEmailAddress("grace@example.com"));
    }
}
