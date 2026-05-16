package com.resiflow.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    private static final Pattern PASSWORD_UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_SPECIAL_CHARACTER_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");

    public void validateNewPassword(final String newPassword, final String confirmPassword) {
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        if (isBlank(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation must not be blank");
        }

        String normalizedPassword = newPassword.trim();
        String normalizedConfirmation = confirmPassword.trim();

        if (!normalizedPassword.equals(normalizedConfirmation)) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
        if (normalizedPassword.length() < 8) {
            throw new IllegalArgumentException("New password must contain at least 8 characters");
        }
        if (!PASSWORD_UPPERCASE_PATTERN.matcher(normalizedPassword).matches()) {
            throw new IllegalArgumentException("New password must contain at least one uppercase letter");
        }
        if (!PASSWORD_LOWERCASE_PATTERN.matcher(normalizedPassword).matches()) {
            throw new IllegalArgumentException("New password must contain at least one lowercase letter");
        }
        if (!PASSWORD_SPECIAL_CHARACTER_PATTERN.matcher(normalizedPassword).matches()) {
            throw new IllegalArgumentException("New password must contain at least one special character");
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
