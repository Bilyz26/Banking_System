package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.CreateCustomerCommand;
import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.application.port.in.CreateCustomerUseCase;
import com.bankingsystem.customer.application.port.out.CustomerIdGenerator;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;

import java.util.Objects;

/**
 * Application service coordinating customer registration.
 */
public final class CreateCustomerService implements CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerIdGenerator customerIdGenerator;

    public CreateCustomerService(
            CustomerRepository customerRepository,
            CustomerIdGenerator customerIdGenerator) {
        this.customerRepository =
                Objects.requireNonNull(customerRepository, "customer repository must not be null");
        this.customerIdGenerator =
                Objects.requireNonNull(customerIdGenerator, "customer id generator must not be null");
    }

    @Override
    public CreateCustomerResult createCustomer(CreateCustomerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Customer customer = new Customer(
                customerIdGenerator.generate(),
                command.fullName(),
                command.emailAddress());

        if (customerRepository.existsByEmailAddress(customer.emailAddress())) {
            throw new DuplicateCustomerEmailException(customer.emailAddress());
        }

        customerRepository.save(customer);

        return new CreateCustomerResult(
                customer.id(),
                customer.fullName(),
                customer.emailAddress());
    }
}

