package com.resiflow.service;

import com.resiflow.config.CaptchaProperties;
import com.resiflow.dto.ApiErrorCode;
import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.JwtProperties;
import com.resiflow.security.JwtService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

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
                repositoryProxy(Optional.of(user), new AtomicReference<>()),
                residenceServiceStub(),
                logementServiceStub(),
                jwtService,
                passwordEncoder,
                captchaServiceDisabled(),
                eventPublisherNoOp()
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
    void loginRejectsPendingUser() {
        User user = new User();
        user.setEmail("resident@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        AuthService authService = new AuthService(
                repositoryProxy(Optional.of(user), new AtomicReference<>()),
                residenceServiceStub(),
                logementServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                captchaServiceDisabled(),
                eventPublisherNoOp()
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
    void registerCreatesPendingResidenceUserOnSelectedLogement() {
        AtomicReference<User> savedUserRef = new AtomicReference<>();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), savedUserRef),
                residenceServiceStub(),
                logementServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                captchaServiceDisabled(),
                eventPublisher
        );

        RegisterRequest request = new RegisterRequest();
        request.setEmail(" resident@example.com ");
        request.setFirstName(" Lea ");
        request.setLastName(" Martin ");
        request.setPassword(" secret ");
        request.setResidenceCode(" RES-ABC123 ");
        request.setLogementId(70L);
        request.setCaptchaToken("captcha-token");

        User result = authService.register(request);

        assertThat(savedUserRef.get().getEmail()).isEqualTo("resident@example.com");
        assertThat(savedUserRef.get().getFirstName()).isEqualTo("Lea");
        assertThat(savedUserRef.get().getLastName()).isEqualTo("Martin");
        assertThat(passwordEncoder.matches("secret", savedUserRef.get().getPassword())).isTrue();
        assertThat(savedUserRef.get().getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUserRef.get().getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(savedUserRef.get().getResidence().getCode()).isEqualTo("RES-ABC123");
        assertThat(savedUserRef.get().getLogementId()).isEqualTo(70L);
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(eventPublisher.lastEvent).isInstanceOf(RegistrationCompletedEvent.class);
    }

    @Test
    void registerRejectsMissingLogement() {
        AuthService authService = new AuthService(
                repositoryProxy(Optional.empty(), new AtomicReference<>()),
                residenceServiceStub(),
                logementServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                captchaServiceDisabled(),
                eventPublisherNoOp()
        );

        RegisterRequest request = new RegisterRequest();
        request.setEmail("resident@example.com");
        request.setFirstName("Lea");
        request.setLastName("Martin");
        request.setPassword("secret");
        request.setResidenceCode("RES-ABC123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Logement ID must not be null");
    }

    @Test
    void registerTranslatesDuplicateEmailConstraintViolation() {
        AuthService authService = new AuthService(
                repositoryProxyThrowingOnSaveAndFlush(
                        Optional.empty(),
                        new DataIntegrityViolationException(
                                "duplicate",
                                new RuntimeException("ERROR: duplicate key value violates unique constraint \"uk_users_email\"")
                        )
                ),
                residenceServiceStub(),
                logementServiceStub(),
                new JwtService(new JwtProperties(SECRET, 3600000)),
                passwordEncoder,
                captchaServiceDisabled(),
                eventPublisherNoOp()
        );

        RegisterRequest request = new RegisterRequest();
        request.setEmail("resident@example.com");
        request.setFirstName("Lea");
        request.setLastName("Martin");
        request.setPassword("secret");
        request.setResidenceCode("RES-ABC123");
        request.setLogementId(70L);
        request.setCaptchaToken("captcha-token");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessage("Email is already used");
    }

    private UserRepository repositoryProxy(
            final Optional<User> userToReturn,
            final AtomicReference<User> savedUserRef
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findByEmail".equals(method.getName())) {
                return userToReturn;
            }
            if ("existsByEmail".equals(method.getName())) {
                return userToReturn.isPresent();
            }
            if ("saveAndFlush".equals(method.getName())) {
                User user = (User) args[0];
                savedUserRef.set(user);
                User saved = new User();
                saved.setId(99L);
                saved.setEmail(user.getEmail());
                saved.setFirstName(user.getFirstName());
                saved.setLastName(user.getLastName());
                saved.setPassword(user.getPassword());
                saved.setResidence(user.getResidence());
                saved.setLogement(user.getLogement());
                saved.setRole(user.getRole());
                saved.setStatus(user.getStatus());
                saved.setCreatedAt(user.getCreatedAt());
                saved.setUpdatedAt(user.getUpdatedAt());
                return saved;
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

    private UserRepository repositoryProxyThrowingOnSaveAndFlush(
            final Optional<User> userToReturn,
            final RuntimeException saveException
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findByEmail".equals(method.getName())) {
                return userToReturn;
            }
            if ("existsByEmail".equals(method.getName())) {
                return userToReturn.isPresent();
            }
            if ("saveAndFlush".equals(method.getName())) {
                throw saveException;
            }
            if ("toString".equals(method.getName())) {
                return "UserRepositorySaveAndFlushFailureProxy";
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

    private LogementService logementServiceStub() {
        return new LogementService(null, null, null, null) {
            @Override
            public Logement getRequiredLogement(final Long logementId) {
                Residence residence = new Residence();
                residence.setId(12L);
                residence.setCode("RES-ABC123");

                Logement logement = new Logement();
                logement.setId(logementId);
                logement.setResidence(residence);
                logement.setNumero("A101");
                logement.setTypeLogement(TypeLogement.APPARTEMENT);
                logement.setActive(Boolean.FALSE);
                return logement;
            }

            @Override
            public void ensureLogementCanAcceptRegistration(final Long logementId) {
            }

            @Override
            public void ensureLogementBelongsToResidence(final Long logementId, final Long residenceId) {
            }
        };
    }

    private CaptchaVerificationService captchaServiceDisabled() {
        return new CaptchaVerificationService(
                new CaptchaProperties(false, "", "", ""),
                RestClient.builder().build()
        );
    }

    private ApplicationEventPublisher eventPublisherNoOp() {
        return event -> {
        };
    }

    private static final class RecordingEventPublisher implements ApplicationEventPublisher {

        private Object lastEvent;

        @Override
        public void publishEvent(final Object event) {
            this.lastEvent = event;
        }
    }
}
