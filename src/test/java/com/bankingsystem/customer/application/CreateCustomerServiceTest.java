package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.CreateCustomerCommand;
import com.bankingsystem.customer.application.port.in.CreateCustomerResult;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateCustomerServiceTest {

    private static final CustomerId FIXED_ID =
            new CustomerId(UUID.fromString("b37d0f33-21e9-47dc-9c40-247d36b2d57d"));

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final CreateCustomerService service =
            new CreateCustomerService(repository, () -> FIXED_ID);

    @Test
    void createsAndStoresANormalizedCustomer() {
        CreateCustomerResult result = service.createCustomer(
                new CreateCustomerCommand("  Ada Lovelace ", " ADA@EXAMPLE.COM "));

        assertEquals(FIXED_ID, result.customerId());
        assertEquals("Ada Lovelace", result.fullName());
        assertEquals("ada@example.com", result.emailAddress());
        assertEquals(1, repository.customersByEmail.size());
    }

    @Test
    void rejectsAnEmailAddressAlreadyInUse() {
        service.createCustomer(new CreateCustomerCommand("Ada", "ada@example.com"));

        assertThrows(
                DuplicateCustomerEmailException.class,
                () -> service.createCustomer(
                        new CreateCustomerCommand("Another Ada", "ADA@EXAMPLE.COM")));
        assertEquals(1, repository.customersByEmail.size());
    }

    private static final class InMemoryCustomerRepository implements CustomerRepository {

        private final Map<String, Customer> customersByEmail = new HashMap<>();

        @Override
        public boolean existsByEmailAddress(String emailAddress) {
            return customersByEmail.containsKey(emailAddress);
        }

        @Override
        public void save(Customer customer) {
            customersByEmail.put(customer.emailAddress(), customer);
        }
    }
}
