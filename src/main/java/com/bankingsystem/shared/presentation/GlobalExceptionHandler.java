package com.bankingsystem.shared.presentation;

import com.bankingsystem.account.application.AccountNotFoundException;
import com.bankingsystem.account.application.ConcurrentAccountModificationException;
import com.bankingsystem.customer.application.CustomerNotFoundException;
import com.bankingsystem.customer.application.DuplicateCustomerEmailException;
import com.bankingsystem.shared.application.ApplicationException;
import com.bankingsystem.shared.domain.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public final class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCustomerEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(
            DuplicateCustomerEmailException exception) {
        return error(HttpStatus.CONFLICT, "CUSTOMER_EMAIL_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(ConcurrentAccountModificationException.class)
    public ResponseEntity<ApiError> handleConcurrentAccountModification(
            ConcurrentAccountModificationException exception) {
        return error(
                HttpStatus.CONFLICT,
                "CONCURRENT_ACCOUNT_MODIFICATION",
                exception.getMessage());
    }

    @ExceptionHandler({AccountNotFoundException.class, CustomerNotFoundException.class})
    public ResponseEntity<ApiError> handleResourceNotFound(ApplicationException exception) {
        String errorCode = exception instanceof AccountNotFoundException
                ? "ACCOUNT_NOT_FOUND"
                : "CUSTOMER_NOT_FOUND";
        return error(HttpStatus.NOT_FOUND, errorCode, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleMalformedRequest(Exception exception) {
        return error(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Request body or path parameter is malformed");
    }

    @ExceptionHandler({IllegalArgumentException.class, DomainException.class})
    public ResponseEntity<ApiError> handleInvalidRequest(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiError> handleApplicationFailure(ApplicationException exception) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "APPLICATION_RULE_VIOLATION", exception.getMessage());
    }

    private static ResponseEntity<ApiError> error(
            HttpStatus status,
            String code,
            String message) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                Map.of()));
    }
}
