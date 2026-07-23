package com.bankingsystem.customer.application.port.out;

import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;

import java.util.Optional;

public interface CustomerRepository {

    Optional<Customer> findById(CustomerId customerId);

    boolean existsByEmailAddress(String emailAddress);

    void save(Customer customer);
}
