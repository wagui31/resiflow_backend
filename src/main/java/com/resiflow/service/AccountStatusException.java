package com.resiflow.service;

import com.resiflow.dto.ApiErrorCode;

public class AccountStatusException extends RuntimeException {

    private final ApiErrorCode code;

    public AccountStatusException(final ApiErrorCode code, final String message) {
        super(message);
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }
}
