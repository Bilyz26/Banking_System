package com.bankingsystem.customer.domain;

import java.util.Objects;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * An immutable bank customer identity.
 */
public record Customer(CustomerId id, String fullName, String emailAddress) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Customer {
        Objects.requireNonNull(id, "customer id must not be null");
        fullName = requireText(fullName, "full name");
        emailAddress = requireText(emailAddress, "email address").toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new IllegalArgumentException("email address is invalid");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
