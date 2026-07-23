package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.GetCustomerQuery;
import com.bankingsystem.customer.application.port.in.GetCustomerResult;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetCustomerServiceTest {

    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d"));

    private final TestCustomerRepository repository = new TestCustomerRepository();
    private final GetCustomerService service = new GetCustomerService(repository);

    @Test
    void returnsCustomerDetails() {
        repository.save(new Customer(CUSTOMER_ID, "Ada Lovelace", "ada@example.com"));

        GetCustomerResult result =
                service.getCustomer(new GetCustomerQuery(CUSTOMER_ID));

        assertEquals(CUSTOMER_ID, result.customerId());
        assertEquals("Ada Lovelace", result.fullName());
        assertEquals("ada@example.com", result.emailAddress());
    }

    @Test
    void rejectsUnknownCustomer() {
        assertThrows(
                CustomerNotFoundException.class,
                () -> service.getCustomer(new GetCustomerQuery(CUSTOMER_ID)));
    }

    private static final class TestCustomerRepository implements CustomerRepository {

        private final Map<CustomerId, Customer> customers = new HashMap<>();

        @Override
        public Optional<Customer> findById(CustomerId customerId) {
            return Optional.ofNullable(customers.get(customerId));
        }

        @Override
        public boolean existsByEmailAddress(String emailAddress) {
            return customers.values().stream()
                    .anyMatch(customer -> customer.emailAddress().equals(emailAddress));
        }

        @Override
        public void save(Customer customer) {
            customers.put(customer.id(), customer);
        }
    }
}

