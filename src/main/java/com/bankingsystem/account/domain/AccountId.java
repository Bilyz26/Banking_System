package com.bankingsystem.account.domain;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "account id must not be null");
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }
}

