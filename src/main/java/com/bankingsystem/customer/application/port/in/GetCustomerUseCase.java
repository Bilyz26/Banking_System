package com.bankingsystem.customer.application.port.in;

public interface GetCustomerUseCase {

    GetCustomerResult getCustomer(GetCustomerQuery query);
}

