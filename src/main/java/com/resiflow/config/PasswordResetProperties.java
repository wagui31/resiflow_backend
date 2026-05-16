package com.resiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.password-reset")
public record PasswordResetProperties(
        int codeExpirationMinutes,
        int resetSessionExpirationMinutes,
        int maxAttempts,
        int maxResends,
        int resendCooldownSeconds,
        int cleanupRetentionDays
) {
}
