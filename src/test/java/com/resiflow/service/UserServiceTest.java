package com.resiflow.service;

import com.resiflow.dto.AdminUserActionRequest;
import com.resiflow.dto.CreateAdminRequest;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createAdminCreatesActiveResidenceAdmin() {
        AtomicReference<User> savedUserRef = new AtomicReference<>();
        UserService userService = new UserService(
                repositoryProxy(savedUserRef, Optional.empty(), Collections.emptyList(), Collections.emptyList()),
                passwordEncoder,
                residenceServiceStub(),
                new EmailService()
        );

        CreateAdminRequest request = new CreateAdminRequest();
        request.setEmail(" admin@example.com ");
        request.setPassword(" secret ");
        request.setResidenceId(7L);

        User result = userService.createAdmin(request);

        assertThat(savedUserRef.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUserRef.get().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedUserRef.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUserRef.get().getResidenceId()).isEqualTo(7L);
        assertThat(passwordEncoder.matches("secret", savedUserRef.get().getPassword())).isTrue();
        assertThat(savedUserRef.get().getCreatedAt()).isNotNull();
        assertThat(savedUserRef.get().getUpdatedAt()).isNotNull();
        assertThat(savedUserRef.get().getUpdatedAt()).isEqualTo(savedUserRef.get().getCreatedAt());
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getPendingUsersReturnsResidenceScopedUsersForAdmin() {
        User pendingUser = buildUser(14L, "pending@example.com", 7L, UserRole.USER, UserStatus.PENDING);
        UserService userService = new UserService(
                repositoryProxy(new AtomicReference<>(), Optional.empty(), List.of(pendingUser), Collections.emptyList()),
                passwordEncoder,
                residenceServiceStub(),
                new EmailService()
        );

        List<User> result = userService.getPendingUsers(new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN));

        assertThat(result).containsExactly(pendingUser);
    }

    @Test
    void approveUserActivatesPendingUserInSameResidence() {
        AtomicReference<User> savedUserRef = new AtomicReference<>();
        User pendingUser = buildUser(14L, "pending@example.com", 7L, UserRole.USER, UserStatus.PENDING);
        RecordingEmailService emailService = new RecordingEmailService();
        UserService userService = new UserService(
                repositoryProxy(savedUserRef, Optional.of(pendingUser), Collections.emptyList(), Collections.emptyList()),
                passwordEncoder,
                residenceServiceStub(),
                emailService
        );

        User result = userService.approveUser(
                14L,
                new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN),
                new AdminUserActionRequest()
        );

        assertThat(savedUserRef.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUserRef.get().getUpdatedAt()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(emailService.userRecipient).isEqualTo("pending@example.com");
        assertThat(emailService.userSubject).isEqualTo("Votre compte ResiFlow est valide");
        assertThat(emailService.userBody).contains("Vous pouvez vous connecter");
    }

    @Test
    void approveUserRejectsAlreadyActiveUser() {
        User activeUser = buildUser(14L, "active@example.com", 7L, UserRole.USER, UserStatus.ACTIVE);
        UserService userService = new UserService(
                repositoryProxy(new AtomicReference<>(), Optional.of(activeUser), Collections.emptyList(), Collections.emptyList()),
                passwordEncoder,
                residenceServiceStub(),
                new EmailService()
        );

        assertThatThrownBy(() -> userService.approveUser(
                14L,
                new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN),
                new AdminUserActionRequest()
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Compte déjà validé");
    }

    @Test
    void rejectUserForbidsAdminManagingAnotherAdmin() {
        User managedAdmin = buildUser(20L, "other-admin@example.com", 7L, UserRole.ADMIN, UserStatus.PENDING);
        UserService userService = new UserService(
                repositoryProxy(new AtomicReference<>(), Optional.of(managedAdmin), Collections.emptyList(), Collections.emptyList()),
                passwordEncoder,
                residenceServiceStub(),
                new EmailService()
        );

        assertThatThrownBy(() -> userService.rejectUser(
                20L,
                new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN),
                null
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Residence admins cannot manage other admins");
    }

    private UserRepository repositoryProxy(
            final AtomicReference<User> savedUserRef,
            final Optional<User> managedUser,
            final List<User> pendingUsers,
            final List<User> allUsers
    ) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        User user = (User) args[0];
                        savedUserRef.set(user);

                        User savedUser = new User();
                        savedUser.setId(1L);
                        savedUser.setEmail(user.getEmail());
                        savedUser.setPassword(user.getPassword());
                        savedUser.setResidence(user.getResidence());
                        savedUser.setRole(user.getRole());
                        savedUser.setStatus(user.getStatus());
                        savedUser.setCreatedAt(user.getCreatedAt());
                        savedUser.setUpdatedAt(user.getUpdatedAt());
                        return savedUser;
                    }
                    if ("existsByEmail".equals(method.getName())) {
                        return false;
                    }
                    if ("findAll".equals(method.getName())) {
                        return allUsers;
                    }
                    if ("findAllByResidence_IdAndStatus".equals(method.getName())
                            || "findAllByStatus".equals(method.getName())) {
                        return pendingUsers;
                    }
                    if ("findByIdAndResidence_Id".equals(method.getName()) || "findById".equals(method.getName())) {
                        return managedUser;
                    }
                    if ("findByIdAndResidence_IdForUpdate".equals(method.getName())
                            || "findByIdForUpdate".equals(method.getName())) {
                        return managedUser;
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                });
    }

    private ResidenceService residenceServiceStub() {
        return new ResidenceService(null) {
            @Override
            public Residence getRequiredResidence(final Long residenceId) {
                Residence residence = new Residence();
                residence.setId(residenceId);
                residence.setCode("RES-ABC123");
                return residence;
            }
        };
    }

    private User buildUser(
            final Long id,
            final String email,
            final Long residenceId,
            final UserRole role,
            final UserStatus status
    ) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setResidenceId(residenceId);
        user.setRole(role);
        user.setStatus(status);
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    private static final class RecordingEmailService extends EmailService {

        private final List<String> adminRecipients = new ArrayList<>();
        private String userRecipient;
        private String userSubject;
        private String userBody;

        @Override
        public void sendToAdmins(final List<String> recipients, final String subject, final String body) {
            adminRecipients.clear();
            adminRecipients.addAll(recipients);
        }

        @Override
        public void sendToUser(final String recipient, final String subject, final String body) {
            userRecipient = recipient;
            userSubject = subject;
            userBody = body;
        }
    }
}
