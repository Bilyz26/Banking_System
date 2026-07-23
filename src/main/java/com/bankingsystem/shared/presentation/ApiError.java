package com.bankingsystem.shared.presentation;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        Map<String, String> fieldErrors) {

    public ApiError {
        fieldErrors = Map.copyOf(fieldErrors);
    }
}

