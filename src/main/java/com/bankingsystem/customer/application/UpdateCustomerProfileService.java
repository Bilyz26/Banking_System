package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileCommand;
import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileResult;
import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileUseCase;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;

import java.util.Objects;

public final class UpdateCustomerProfileService implements UpdateCustomerProfileUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository =
                Objects.requireNonNull(customerRepository, "customer repository must not be null");
    }

    @Override
    public UpdateCustomerProfileResult updateCustomerProfile(
            UpdateCustomerProfileCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.customerId(), "customer id must not be null");

        Customer existingCustomer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));
        Customer updatedCustomer = existingCustomer.updateProfile(
                command.fullName(),
                command.emailAddress());

        boolean emailChanged =
                !existingCustomer.emailAddress().equals(updatedCustomer.emailAddress());
        if (emailChanged
                && customerRepository.existsByEmailAddress(updatedCustomer.emailAddress())) {
            throw new DuplicateCustomerEmailException(updatedCustomer.emailAddress());
        }

        customerRepository.save(updatedCustomer);

        return new UpdateCustomerProfileResult(
                updatedCustomer.id(),
                updatedCustomer.fullName(),
                updatedCustomer.emailAddress());
    }
}

