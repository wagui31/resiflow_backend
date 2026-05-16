package com.resiflow.service;

public class PasswordResetSessionInvalidException extends RuntimeException {

    public PasswordResetSessionInvalidException(final String message) {
        super(message);
    }
}
