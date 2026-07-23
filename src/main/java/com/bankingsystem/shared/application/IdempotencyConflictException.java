package com.bankingsystem.shared.application;

public final class IdempotencyConflictException extends ApplicationException {

    public IdempotencyConflictException() {
        super("The idempotency key was already used for a different operation");
    }
}
