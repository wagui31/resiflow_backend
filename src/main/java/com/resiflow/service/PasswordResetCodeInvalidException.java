package com.resiflow.service;

public class PasswordResetCodeInvalidException extends RuntimeException {

    public PasswordResetCodeInvalidException(final String message) {
        super(message);
    }
}
