package com.resiflow.service;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(final String message) {
        super(message);
    }
}
