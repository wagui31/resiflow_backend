package com.resiflow.controller;

import com.resiflow.dto.ApiErrorCode;
import com.resiflow.dto.ApiErrorResponse;
import com.resiflow.service.AccountStatusException;
import com.resiflow.service.CaptchaValidationException;
import com.resiflow.service.EmailAlreadyUsedException;
import com.resiflow.service.InvalidCredentialsException;
import com.resiflow.service.InvalidResidenceCodeException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(final IllegalArgumentException exception) {
        LOGGER.warn("Validation error: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(final IllegalStateException exception) {
        LOGGER.warn("Business rule violation: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, exception.getMessage());
    }

    @ExceptionHandler(CaptchaValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleCaptchaValidationException(final CaptchaValidationException exception) {
        LOGGER.warn("Captcha validation failed: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_CAPTCHA, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(final InvalidCredentialsException exception) {
        LOGGER.warn("Authentication failed: {}", exception.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, exception.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountStatusException(final AccountStatusException exception) {
        LOGGER.warn("Account status rejected request: code={} message={}", exception.getCode(), exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(final AccessDeniedException exception) {
        LOGGER.warn("Access denied: {}", exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyUsedException(final EmailAlreadyUsedException exception) {
        LOGGER.warn("Registration rejected because email is already used: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.EMAIL_ALREADY_USED, exception.getMessage());
    }

    @ExceptionHandler(InvalidResidenceCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidResidenceCodeException(final InvalidResidenceCodeException exception) {
        LOGGER.warn("Invalid residence code: {}", exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ApiErrorCode.INVALID_RESIDENCE_CODE, exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNoSuchElementException(final NoSuchElementException exception) {
        LOGGER.warn("Resource not found: {}", exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            final HttpStatus status,
            final ApiErrorCode code,
            final String message
    ) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(code, message, status.value()));
    }
}
