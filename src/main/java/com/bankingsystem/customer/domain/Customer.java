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
    private static final int MAX_FULL_NAME_LENGTH = 200;
    private static final int MAX_EMAIL_ADDRESS_LENGTH = 320;

    public Customer {
        Objects.requireNonNull(id, "customer id must not be null");
        fullName = requireText(fullName, "full name");
        emailAddress = requireText(emailAddress, "email address").toLowerCase(Locale.ROOT);

        requireMaximumLength(fullName, MAX_FULL_NAME_LENGTH, "full name");
        requireMaximumLength(emailAddress, MAX_EMAIL_ADDRESS_LENGTH, "email address");

        if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new InvalidCustomerProfileException("email address is invalid");
        }
    }

    public Customer updateProfile(String fullName, String emailAddress) {
        return new Customer(id, fullName, emailAddress);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerProfileException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static void requireMaximumLength(
            String value,
            int maximumLength,
            String fieldName) {
        if (value.length() > maximumLength) {
            throw new InvalidCustomerProfileException(
                    "%s must not exceed %d characters".formatted(fieldName, maximumLength));
        }
    }
}
