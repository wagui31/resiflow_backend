package com.resiflow.dto;

public class ForgotPasswordRequestCodeResponse {

    private final String message;

    public ForgotPasswordRequestCodeResponse(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
