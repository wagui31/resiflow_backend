package com.resiflow.service;

import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.dto.ApiErrorCode;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.JwtProperties;
import com.resiflow.security.JwtService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private static final String SECRET = "Zm9yLXRlc3RzLW9ubHktcmVzaWZsb3ctand0LXNlY3JldC1rZXktMzItYnl0ZXM=";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void loginReturnsUserDataWhenCredentialsAreValid() {
        User user = new User();
        user.setId(4L);
        user.setEmail("resident@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setResidenceId(12L);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3600000));
        AuthService authService = new AuthService(
                repositoryProxy(Optional.of(user), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                jwtService,
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail(" resident@example.com ");
        request.setPassword(" secret ");

        LoginResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo(4L);
        assertThat(response.getEmail()).isEqualTo("resident@example.com");
        assertThat(response.getResidenceId()).isEqualTo(12L);
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getToken()).isNotBlank();
        assertThat(jwtService.extractSubject(response.getToken())).isEqualTo("resident@example.com");
    }

    @Test
    void loginRejectsBlankEmail() {
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail(" ");
        request.setPassword("secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be blank");
    }

    @Test
    void loginRejectsNullRequest() {
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Login request must not be null");
    }

    @Test
    void loginRejectsBlankPassword() {
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword(" ");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must not be blank");
    }

    @Test
    void loginRejectsUnknownEmail() {
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword("secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User();
        user.setEmail("resident@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        AuthService authService = new AuthService(
                repositoryProxy(Optional.of(user), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void loginRejectsPendingUser() {
        User user = new User();
        user.setEmail("resident@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        AuthService authService = new AuthService(
                repositoryProxy(Optional.of(user), new AtomicReference<>(), List.of()),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                new EmailService()
        );

        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword("secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountStatusException.class)
                .hasMessage("Votre compte est en attente de validation")
                .extracting(error -> ((AccountStatusException) error).getCode())
                .isEqualTo(ApiErrorCode.ACCOUNT_PENDING);
    }

    @Test
    void registerCreatesPendingResidenceUser() {
        AtomicReference<User> savedUserRef = new AtomicReference<>();
        RecordingEmailService emailService = new RecordingEmailService();
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), savedUserRef, List.of("admin@example.com")),
                residenceServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                emailService
        );

        RegisterRequest request = new RegisterRequest();
        request.setEmail(" resident@example.com ");
        request.setPassword(" secret ");
        request.setResidenceCode(" RES-ABC123 ");
        request.setNumeroImmeuble(" B ");
        request.setCodeLogement(" 12A ");

        User result = authService.register(request);

        assertThat(savedUserRef.get().getEmail()).isEqualTo("resident@example.com");
        assertThat(passwordEncoder.matches("secret", savedUserRef.get().getPassword())).isTrue();
        assertThat(savedUserRef.get().getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUserRef.get().getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(savedUserRef.get().getResidence().getCode()).isEqualTo("RES-ABC123");
        assertThat(savedUserRef.get().getNumeroImmeuble()).isEqualTo("B");
        assertThat(savedUserRef.get().getCodeLogement()).isEqualTo("12A");
        assertThat(savedUserRef.get().getCreatedAt()).isNotNull();
        assertThat(savedUserRef.get().getUpdatedAt()).isNotNull();
        assertThat(savedUserRef.get().getUpdatedAt()).isEqualTo(savedUserRef.get().getCreatedAt());
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(emailService.adminRecipients).containsExactly("admin@example.com");
        assertThat(emailService.adminSubject).isEqualTo("Nouvelle demande d'inscription");
        assertThat(emailService.adminBody).contains("resident@example.com");
    }

    private UserRepository repositoryProxy(
            final Optional<User> userToReturn,
            final AtomicReference<User> savedUserRef,
            final List<String> adminEmails
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findByEmail".equals(method.getName())) {
                return userToReturn;
            }
            if ("existsByEmail".equals(method.getName())) {
                return userToReturn.isPresent();
            }
            if ("save".equals(method.getName())) {
                User user = (User) args[0];
                savedUserRef.set(user);
                User saved = new User();
                saved.setId(99L);
                saved.setEmail(user.getEmail());
                saved.setPassword(user.getPassword());
                saved.setResidence(user.getResidence());
                saved.setNumeroImmeuble(user.getNumeroImmeuble());
                saved.setCodeLogement(user.getCodeLogement());
                saved.setRole(user.getRole());
                saved.setStatus(user.getStatus());
                saved.setCreatedAt(user.getCreatedAt());
                saved.setUpdatedAt(user.getUpdatedAt());
                return saved;
            }
            if ("findAllByResidence_IdAndRole".equals(method.getName())) {
                return adminEmails.stream().map(email -> {
                    User admin = new User();
                    admin.setEmail(email);
                    admin.setRole(UserRole.ADMIN);
                    admin.setStatus(UserStatus.ACTIVE);
                    return admin;
                }).toList();
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
        };

        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                handler);
    }

    private ResidenceService residenceServiceStub() {
        return new ResidenceService(null) {
            @Override
            public Residence getRequiredResidenceByCode(final String residenceCode) {
                Residence residence = new Residence();
                residence.setId(12L);
                residence.setCode(residenceCode);
                LocalDateTime now = LocalDateTime.now().minusDays(1);
                residence.setCreatedAt(now);
                residence.setUpdatedAt(now);
                return residence;
            }
        };
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
