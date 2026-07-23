package com.bankingsystem.customer.application.port.out;

import com.bankingsystem.customer.domain.CustomerId;

@FunctionalInterface
public interface CustomerIdGenerator {

    CustomerId generate();
}

