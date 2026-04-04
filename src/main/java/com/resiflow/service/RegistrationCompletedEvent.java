package com.resiflow.service;

public record RegistrationCompletedEvent(
        Long residenceId,
        String email
) {
}
