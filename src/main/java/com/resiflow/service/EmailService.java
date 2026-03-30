package com.resiflow.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public EmailService() {
        this((JavaMailSender) null, false, "noreply@resiflow.local");
    }

    @Autowired
    public EmailService(
            final ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") final boolean enabled,
            @Value("${app.mail.from:noreply@resiflow.local}") final String fromAddress
    ) {
        this(mailSenderProvider.getIfAvailable(), enabled, fromAddress);
    }

    EmailService(final JavaMailSender mailSender, final boolean enabled, final String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public void sendToAdmins(final List<String> recipients, final String subject, final String body) {
        if (recipients == null || recipients.isEmpty()) {
            LOGGER.info("Skipping admin email because there are no recipients for subject={}", subject);
            return;
        }
        sendEmail(recipients.toArray(String[]::new), subject, body);
    }

    public void sendToUser(final String recipient, final String subject, final String body) {
        if (recipient == null || recipient.trim().isEmpty()) {
            LOGGER.info("Skipping user email because recipient is blank for subject={}", subject);
            return;
        }
        sendEmail(new String[]{recipient.trim()}, subject, body);
    }

    private void sendEmail(final String[] recipients, final String subject, final String body) {
        if (!enabled) {
            LOGGER.info("Email disabled recipients={} subject={} body={}", List.of(recipients), subject, body);
            return;
        }
        if (mailSender == null) {
            throw new IllegalStateException("Mail service is enabled but no mail sender is configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipients);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            LOGGER.info("Email sent recipients={} subject={}", List.of(recipients), subject);
        } catch (MailException exception) {
            throw new IllegalStateException("Failed to send email", exception);
        }
    }
}
