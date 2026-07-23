package com.bankingsystem.account.application.port.out;

import com.bankingsystem.customer.domain.CustomerId;

@FunctionalInterface
public interface CustomerExistenceChecker {

    boolean exists(CustomerId customerId);
}

