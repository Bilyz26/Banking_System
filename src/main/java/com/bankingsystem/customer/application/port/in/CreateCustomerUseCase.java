package com.bankingsystem.customer.application.port.in;

public interface CreateCustomerUseCase {

    CreateCustomerResult createCustomer(CreateCustomerCommand command);
}

