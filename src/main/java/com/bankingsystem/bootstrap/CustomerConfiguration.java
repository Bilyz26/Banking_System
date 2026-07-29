package com.bankingsystem.bootstrap;

import com.bankingsystem.customer.application.CreateCustomerService;
import com.bankingsystem.customer.application.GetCustomerService;
import com.bankingsystem.customer.application.UpdateCustomerProfileService;
import com.bankingsystem.customer.application.port.in.CreateCustomerUseCase;
import com.bankingsystem.customer.application.port.in.GetCustomerUseCase;
import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileUseCase;
import com.bankingsystem.customer.application.port.out.CustomerIdGenerator;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.CustomerId;
import com.bankingsystem.customer.infrastructure.InMemoryCustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CustomerConfiguration {

    @Bean
    @Profile("!postgresql")
    CustomerRepository customerRepository() {
        return new InMemoryCustomerRepository();
    }

    @Bean
    CustomerIdGenerator customerIdGenerator() {
        return CustomerId::generate;
    }

    @Bean
    CreateCustomerUseCase createCustomerUseCase(
            CustomerRepository customerRepository,
            CustomerIdGenerator customerIdGenerator) {
        return new CreateCustomerService(customerRepository, customerIdGenerator);
    }

    @Bean
    GetCustomerUseCase getCustomerUseCase(CustomerRepository customerRepository) {
        return new GetCustomerService(customerRepository);
    }

    @Bean
    UpdateCustomerProfileUseCase updateCustomerProfileUseCase(
            CustomerRepository customerRepository) {
        return new UpdateCustomerProfileService(customerRepository);
    }
}
