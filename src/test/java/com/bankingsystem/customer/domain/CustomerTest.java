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
                InvalidCustomerProfileException.class,
                () -> new Customer(CustomerId.generate(), "Ada Lovelace", "invalid"));
    }

    @Test
    void rejectsBlankCustomerDetails() {
        assertThrows(
                InvalidCustomerProfileException.class,
                () -> new Customer(CustomerId.generate(), " ", "ada@example.com"));
        assertThrows(
                InvalidCustomerProfileException.class,
                () -> new Customer(CustomerId.generate(), "Ada Lovelace", " "));
    }

    @Test
    void enforcesDomainLengthLimits() {
        assertThrows(
                InvalidCustomerProfileException.class,
                () -> new Customer(
                        CustomerId.generate(),
                        "A".repeat(201),
                        "ada@example.com"));

        String oversizedEmail = "a".repeat(309) + "@example.com";
        assertThrows(
                InvalidCustomerProfileException.class,
                () -> new Customer(
                        CustomerId.generate(),
                        "Ada Lovelace",
                        oversizedEmail));
    }
}
