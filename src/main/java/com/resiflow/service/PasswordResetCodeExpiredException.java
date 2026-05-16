package com.resiflow.service;

public class PasswordResetCodeExpiredException extends RuntimeException {

    public PasswordResetCodeExpiredException(final String message) {
        super(message);
    }
}
