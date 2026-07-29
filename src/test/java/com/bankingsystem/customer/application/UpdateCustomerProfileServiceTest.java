package com.bankingsystem.customer.application;

import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileCommand;
import com.bankingsystem.customer.application.port.in.UpdateCustomerProfileResult;
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

class UpdateCustomerProfileServiceTest {

    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("38e375d4-c055-471d-869d-c752f758cc4d"));
    private static final CustomerId OTHER_CUSTOMER_ID =
            new CustomerId(UUID.fromString("97e3e440-e76a-481a-9ce8-918f46ad64e1"));

    private final TestCustomerRepository repository = new TestCustomerRepository();
    private final UpdateCustomerProfileService service =
            new UpdateCustomerProfileService(repository);

    @Test
    void updatesAndNormalizesCustomerProfileWithoutChangingIdentity() {
        repository.save(new Customer(CUSTOMER_ID, "Ada", "ada@example.com"));

        UpdateCustomerProfileResult result = service.updateCustomerProfile(
                new UpdateCustomerProfileCommand(
                        CUSTOMER_ID,
                        "  Ada Lovelace  ",
                        "  ADA.LOVELACE@EXAMPLE.COM "));

        assertEquals(CUSTOMER_ID, result.customerId());
        assertEquals("Ada Lovelace", result.fullName());
        assertEquals("ada.lovelace@example.com", result.emailAddress());
        assertEquals(
                "ada.lovelace@example.com",
                repository.findById(CUSTOMER_ID).orElseThrow().emailAddress());
    }

    @Test
    void permitsKeepingTheExistingEmailAddress() {
        repository.save(new Customer(CUSTOMER_ID, "Ada", "ada@example.com"));

        UpdateCustomerProfileResult result = service.updateCustomerProfile(
                new UpdateCustomerProfileCommand(
                        CUSTOMER_ID,
                        "Ada Lovelace",
                        "ADA@EXAMPLE.COM"));

        assertEquals("ada@example.com", result.emailAddress());
    }

    @Test
    void rejectsEmailOwnedByAnotherCustomerWithoutChangingProfile() {
        repository.save(new Customer(CUSTOMER_ID, "Ada", "ada@example.com"));
        repository.save(
                new Customer(OTHER_CUSTOMER_ID, "Grace", "grace@example.com"));

        assertThrows(
                DuplicateCustomerEmailException.class,
                () -> service.updateCustomerProfile(
                        new UpdateCustomerProfileCommand(
                                CUSTOMER_ID,
                                "Ada Lovelace",
                                "grace@example.com")));

        Customer unchangedCustomer = repository.findById(CUSTOMER_ID).orElseThrow();
        assertEquals("Ada", unchangedCustomer.fullName());
        assertEquals("ada@example.com", unchangedCustomer.emailAddress());
    }

    @Test
    void rejectsUnknownCustomer() {
        assertThrows(
                CustomerNotFoundException.class,
                () -> service.updateCustomerProfile(
                        new UpdateCustomerProfileCommand(
                                CUSTOMER_ID,
                                "Ada",
                                "ada@example.com")));
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
