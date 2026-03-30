package com.resiflow.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private final ApiErrorCode code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;

    public ApiErrorResponse(
            final ApiErrorCode code,
            final String message,
            final int status,
            final LocalDateTime timestamp
    ) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    public static ApiErrorResponse of(final ApiErrorCode code, final String message, final int status) {
        return new ApiErrorResponse(code, message, status, LocalDateTime.now());
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
