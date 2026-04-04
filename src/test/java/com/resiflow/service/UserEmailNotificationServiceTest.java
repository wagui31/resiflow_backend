package com.resiflow.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEmailNotificationServiceTest {

    @Test
    void handleUserEmailNotificationSendsEmailToRecipient() {
        RecordingEmailService emailService = new RecordingEmailService();
        UserEmailNotificationService service = new UserEmailNotificationService(emailService);

        service.handleUserEmailNotification(new UserEmailNotificationEvent(
                "user@example.com",
                "Subject",
                "Body"
        ));

        assertThat(emailService.userRecipient).isEqualTo("user@example.com");
        assertThat(emailService.userSubject).isEqualTo("Subject");
        assertThat(emailService.userBody).isEqualTo("Body");
    }

    private static final class RecordingEmailService extends EmailService {

        private String userRecipient;
        private String userSubject;
        private String userBody;

        @Override
        public void sendToUser(final String recipient, final String subject, final String body) {
            userRecipient = recipient;
            userSubject = subject;
            userBody = body;
        }
    }
}
