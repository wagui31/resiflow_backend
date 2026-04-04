package com.resiflow.service;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class RegistrationNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationNotificationService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;

    public RegistrationNotificationService(
            final UserRepository userRepository,
            final EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCompleted(final RegistrationCompletedEvent event) {
        List<String> adminEmails = userRepository.findAllByResidence_IdAndRole(event.residenceId(), UserRole.ADMIN)
                .stream()
                .map(User::getEmail)
                .toList();

        try {
            emailService.sendToAdmins(
                    adminEmails,
                    "Nouvelle demande d'inscription",
                    "Un nouvel utilisateur a demande l'acces a la residence. Email: " + event.email()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Failed to send registration notification for residenceId={} email={}",
                    event.residenceId(),
                    event.email(),
                    exception
            );
        }
    }
}
