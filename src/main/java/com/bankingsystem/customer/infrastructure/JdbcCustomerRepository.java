package com.bankingsystem.customer.infrastructure;

import com.bankingsystem.customer.application.DuplicateCustomerEmailException;
import com.bankingsystem.customer.application.port.out.CustomerRepository;
import com.bankingsystem.customer.domain.Customer;
import com.bankingsystem.customer.domain.CustomerId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JdbcCustomerRepository implements CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbc template must not be null");
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        Objects.requireNonNull(customerId, "customer id must not be null");
        return jdbcTemplate.query(
                        """
                        SELECT customer_id, full_name, email_address
                        FROM customers
                        WHERE customer_id = ?
                        """,
                        (resultSet, rowNumber) -> new Customer(
                                new CustomerId(resultSet.getObject("customer_id", UUID.class)),
                                resultSet.getString("full_name"),
                                resultSet.getString("email_address")),
                        customerId.value())
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        Objects.requireNonNull(emailAddress, "email address must not be null");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE email_address = ?",
                Integer.class,
                emailAddress);
        return count != null && count > 0;
    }

    @Override
    public void save(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");
        try {
            int updatedRows = jdbcTemplate.update(
                    """
                    UPDATE customers
                    SET full_name = ?, email_address = ?
                    WHERE customer_id = ?
                    """,
                    customer.fullName(),
                    customer.emailAddress(),
                    customer.id().value());
            if (updatedRows == 0) {
                jdbcTemplate.update(
                        """
                        INSERT INTO customers (customer_id, full_name, email_address)
                        VALUES (?, ?, ?)
                        """,
                        customer.id().value(),
                        customer.fullName(),
                        customer.emailAddress());
            }
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCustomerEmailException(customer.emailAddress());
        }
    }
}
