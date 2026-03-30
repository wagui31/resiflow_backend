package com.resiflow.service;

public class InvalidResidenceCodeException extends RuntimeException {

    public InvalidResidenceCodeException(final String message) {
        super(message);
    }
}
