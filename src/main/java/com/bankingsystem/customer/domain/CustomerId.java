package com.bankingsystem.customer.domain;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "customer id must not be null");
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
}

