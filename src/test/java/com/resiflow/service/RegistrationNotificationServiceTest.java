package com.resiflow.service;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationNotificationServiceTest {

    @Test
    void handleRegistrationCompletedSendsEmailToResidenceAdmins() {
        RecordingEmailService emailService = new RecordingEmailService();
        RegistrationNotificationService service = new RegistrationNotificationService(
                repositoryProxy(List.of("admin1@example.com", "admin2@example.com")),
                emailService
        );

        service.handleRegistrationCompleted(new RegistrationCompletedEvent(12L, "resident@example.com"));

        assertThat(emailService.adminRecipients).containsExactly("admin1@example.com", "admin2@example.com");
        assertThat(emailService.adminSubject).isEqualTo("Nouvelle demande d'inscription");
        assertThat(emailService.adminBody).contains("resident@example.com");
    }

    private UserRepository repositoryProxy(final List<String> adminEmails) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findAllByResidence_IdAndRole".equals(method.getName())) {
                assertThat(args[0]).isEqualTo(12L);
                assertThat(args[1]).isEqualTo(UserRole.ADMIN);
                return adminEmails.stream().map(email -> {
                    User admin = new User();
                    admin.setEmail(email);
                    admin.setRole(UserRole.ADMIN);
                    admin.setStatus(UserStatus.ACTIVE);
                    return admin;
                }).toList();
            }
            if ("toString".equals(method.getName())) {
                return "UserRepositoryNotificationProxy";
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            throw new UnsupportedOperationException("Unsupported method: " + method.getName());
        };

        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                handler
        );
    }

    private static final class RecordingEmailService extends EmailService {

        private final List<String> adminRecipients = new ArrayList<>();
        private String adminSubject;
        private String adminBody;

        @Override
        public void sendToAdmins(final List<String> recipients, final String subject, final String body) {
            adminRecipients.clear();
            adminRecipients.addAll(recipients);
            adminSubject = subject;
            adminBody = body;
        }
    }
}
