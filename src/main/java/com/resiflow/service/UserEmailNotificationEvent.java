package com.resiflow.service;

public record UserEmailNotificationEvent(
        String recipient,
        String subject,
        String body
) {
}
