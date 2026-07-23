package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.GetCustomerQuery;
import com.bankingsystem.customer.application.port.in.GetCustomerResult;
import com.bankingsystem.customer.application.port.in.GetCustomerUseCase;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;

import java.util.Objects;

public final class GetCustomerService implements GetCustomerUseCase {

    private final CustomerRepository customerRepository;

    public GetCustomerService(CustomerRepository customerRepository) {
        this.customerRepository =
                Objects.requireNonNull(customerRepository, "customer repository must not be null");
    }

    @Override
    public GetCustomerResult getCustomer(GetCustomerQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(query.customerId(), "customer id must not be null");

        Customer customer = customerRepository.findById(query.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(query.customerId()));

        return new GetCustomerResult(
                customer.id(),
                customer.fullName(),
                customer.emailAddress());
    }
}

