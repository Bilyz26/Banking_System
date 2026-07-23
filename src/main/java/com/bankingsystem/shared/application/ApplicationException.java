package com.bankingsystem.shared.application;

/**
 * Base type for expected failures while coordinating an application use case.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }
}

