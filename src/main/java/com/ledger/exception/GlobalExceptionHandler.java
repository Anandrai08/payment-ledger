package com.ledger.exception;

import com.ledger.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(AccountNotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e, req);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(WalletNotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e, req);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handle(InsufficientBalanceException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e, req);
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<ErrorResponse> handle(AccountFrozenException e, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, e, req);
    }

    @ExceptionHandler(DuplicateIdempotencyException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateIdempotencyException e, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, e, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle(IllegalArgumentException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e, req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handle(IllegalStateException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e, HttpServletRequest req) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e, req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception e, HttpServletRequest req) {
        return build(status, e.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(status.value(), status.getReasonPhrase(),
                        message, req.getRequestURI(), Instant.now()));
    }
}
