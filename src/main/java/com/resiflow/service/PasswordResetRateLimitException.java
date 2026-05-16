package com.resiflow.service;

public class PasswordResetRateLimitException extends RuntimeException {

    public PasswordResetRateLimitException(final String message) {
        super(message);
    }
}
