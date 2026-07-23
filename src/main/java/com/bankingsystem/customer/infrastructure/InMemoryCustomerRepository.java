package com.bankingsystem.customer.infrastructure;

import com.bankingsystem.customer.application.DuplicateCustomerEmailException;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Temporary adapter used until the PostgreSQL repository is introduced.
 */
public final class InMemoryCustomerRepository implements CustomerRepository {

    private final ConcurrentMap<String, Customer> customersByEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        return customersByEmail.containsKey(emailAddress);
    }

    @Override
    public void save(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");
        Customer existing = customersByEmail.putIfAbsent(customer.emailAddress(), customer);
        if (existing != null) {
            throw new DuplicateCustomerEmailException(customer.emailAddress());
        }
    }
}

