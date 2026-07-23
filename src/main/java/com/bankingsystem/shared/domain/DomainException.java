package com.bankingsystem.shared.domain;

/**
 * Base type for expected business-rule violations.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}

