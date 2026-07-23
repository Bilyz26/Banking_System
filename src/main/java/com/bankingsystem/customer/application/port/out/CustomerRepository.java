package com.bankingsystem.customer.application.port.out;

import com.bankingsystem.customer.domain.Customer;

public interface CustomerRepository {

    boolean existsByEmailAddress(String emailAddress);

    void save(Customer customer);
}
