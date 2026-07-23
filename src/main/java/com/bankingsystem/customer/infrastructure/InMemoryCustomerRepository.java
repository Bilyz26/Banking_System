package com.bankingsystem.customer.infrastructure;

import com.bankingsystem.customer.application.DuplicateCustomerEmailException;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fast, non-durable adapter used by the default development profile.
 */
public final class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<CustomerId, Customer> customersById = new HashMap<>();
    private final Map<String, CustomerId> customerIdsByEmail = new HashMap<>();

    @Override
    public synchronized Optional<Customer> findById(CustomerId customerId) {
        Objects.requireNonNull(customerId, "customer id must not be null");
        return Optional.ofNullable(customersById.get(customerId));
    }

    @Override
    public synchronized boolean existsByEmailAddress(String emailAddress) {
        return customerIdsByEmail.containsKey(emailAddress);
    }

    @Override
    public synchronized void save(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");

        CustomerId emailOwner = customerIdsByEmail.get(customer.emailAddress());
        if (emailOwner != null && !emailOwner.equals(customer.id())) {
            throw new DuplicateCustomerEmailException(customer.emailAddress());
        }

        Customer previousCustomer = customersById.put(customer.id(), customer);
        customerIdsByEmail.put(customer.emailAddress(), customer.id());

        if (previousCustomer != null
                && !previousCustomer.emailAddress().equals(customer.emailAddress())) {
            customerIdsByEmail.remove(previousCustomer.emailAddress(), customer.id());
        }
    }
}
