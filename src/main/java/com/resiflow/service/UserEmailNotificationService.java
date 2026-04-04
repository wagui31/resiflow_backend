package com.resiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class UserEmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEmailNotificationService.class);

    private final EmailService emailService;

    public UserEmailNotificationService(final EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserEmailNotification(final UserEmailNotificationEvent event) {
        try {
            emailService.sendToUser(event.recipient(), event.subject(), event.body());
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to send user email notification to={}", event.recipient(), exception);
        }
    }
}
