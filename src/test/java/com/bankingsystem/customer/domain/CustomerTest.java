package com.bankingsystem.customer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void normalizesCustomerDetails() {
        Customer customer = new Customer(
                CustomerId.generate(),
                "  Ada Lovelace  ",
                "  ADA@EXAMPLE.COM ");

        assertEquals("Ada Lovelace", customer.fullName());
        assertEquals("ada@example.com", customer.emailAddress());
    }

    @Test
    void rejectsInvalidEmailAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Customer(CustomerId.generate(), "Ada Lovelace", "invalid"));
    }
}

