package com.resiflow.service;

public class CaptchaValidationException extends RuntimeException {

    public CaptchaValidationException(final String message) {
        super(message);
    }
}
