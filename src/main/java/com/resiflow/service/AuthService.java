package com.resiflow.service;

import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.dto.ApiErrorCode;
import com.resiflow.entity.Logement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.JwtService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String PENDING_MESSAGE = "Votre compte est en attente de validation";
    private static final String REJECTED_MESSAGE = "Votre demande a ete refusee";
    private static final String ARCHIVED_MESSAGE = "Votre compte a ete archive";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ResidenceService residenceService;
    private final LogementService logementService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaVerificationService captchaVerificationService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(
            final UserRepository userRepository,
            final ResidenceService residenceService,
            final LogementService logementService,
            final JwtService jwtService,
            final PasswordEncoder passwordEncoder,
            final CaptchaVerificationService captchaVerificationService,
            final ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.residenceService = residenceService;
        this.logementService = logementService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.captchaVerificationService = captchaVerificationService;
        this.eventPublisher = eventPublisher;
    }

    public LoginResponse login(final LoginRequest request) {
        validateRequest(request);

        String email = request.getEmail().trim();
        String password = request.getPassword().trim();
        LOGGER.info("Authentication attempt started for email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn("Authentication failed because user was not found for email={}", email);
                    return new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            LOGGER.warn("Authentication failed because password did not match for email={} userId={}", email, user.getId());
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            LOGGER.warn("Authentication blocked because account is pending for userId={} email={}", user.getId(), email);
            throw new AccountStatusException(ApiErrorCode.ACCOUNT_PENDING, PENDING_MESSAGE);
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            LOGGER.warn("Authentication blocked because account is rejected for userId={} email={}", user.getId(), email);
            throw new AccountStatusException(ApiErrorCode.ACCOUNT_REJECTED, REJECTED_MESSAGE);
        }
        if (user.getStatus() == UserStatus.ARCHIVED) {
            LOGGER.warn("Authentication blocked because account is archived for userId={} email={}", user.getId(), email);
            throw new AccountStatusException(ApiErrorCode.ACCOUNT_ARCHIVED, ARCHIVED_MESSAGE);
        }

        String token = jwtService.generateToken(user);
        LOGGER.info(
                "Authentication succeeded for userId={} email={} residenceId={} role={}",
                user.getId(),
                user.getEmail(),
                user.getResidenceId(),
                user.getRole()
        );
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getResidenceId(),
                user.getResidence() == null ? null : user.getResidence().getCurrency(),
                user.getRole(),
                user.getStatus(),
                token
        );
    }

    @Transactional
    public User register(final RegisterRequest request) {
        return register(request, null);
    }

    @Transactional
    public User register(final RegisterRequest request, final String clientPlatform) {
        validateRegisterRequest(request);
        String email = request.getEmail().trim();
        LOGGER.info(
                "Registration started for email={} residenceCode={} logementId={} clientPlatform={}",
                email,
                request.getResidenceCode(),
                request.getLogementId(),
                clientPlatform
        );
        if (!isMobileClient(clientPlatform)) {
            captchaVerificationService.validateRegistrationCaptcha(request.getCaptchaToken());
        }

        ensureEmailAvailable(email);

        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        Logement logement = logementService.getRequiredLogement(request.getLogementId());
        logementService.ensureLogementCanAcceptRegistration(logement.getId());
        Long residenceId = residenceService.getRequiredResidenceByCode(request.getResidenceCode().trim().toUpperCase()).getId();
        logementService.ensureLogementBelongsToResidence(logement.getId(), residenceId);
        user.setEmail(email);
        user.setFirstName(normalizeOptionalValue(request.getFirstName()));
        user.setLastName(normalizeOptionalValue(request.getLastName()));
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setResidence(logement.getResidence());
        user.setLogement(logement);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);
        user.setDateEntreeResidence(now.toLocalDate());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            LOGGER.error("Registration persistence failed for email={}", email, exception);
            throw translateRegisterDataIntegrityViolation(exception, email);
        }

        eventPublisher.publishEvent(new RegistrationCompletedEvent(savedUser.getResidenceId(), savedUser.getEmail()));
        LOGGER.info(
                "Registration completed for userId={} email={} residenceId={} logementId={} status={}",
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getResidenceId(),
                savedUser.getLogementId(),
                savedUser.getStatus()
        );

        return savedUser;
    }

    private void validateRequest(final LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request must not be null");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptionalValue(final String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private boolean isMobileClient(final String clientPlatform) {
        if (clientPlatform == null) {
            return false;
        }
        String normalizedPlatform = clientPlatform.trim().toLowerCase();
        return normalizedPlatform.equals("mobile-android") || normalizedPlatform.equals("mobile-ios");
    }

    private void validateRegisterRequest(final RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Register request must not be null");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (isBlank(request.getLastName())) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (isBlank(request.getResidenceCode())) {
            throw new IllegalArgumentException("Residence code must not be blank");
        }
        if (request.getLogementId() == null) {
            throw new IllegalArgumentException("Logement ID must not be null");
        }
    }

    private void ensureEmailAvailable(final String email) {
        if (userRepository.existsByEmail(email)) {
            LOGGER.warn("Registration rejected because email is already used: email={}", email);
            throw new EmailAlreadyUsedException("Email is already used");
        }
    }

    private RuntimeException translateRegisterDataIntegrityViolation(
            final DataIntegrityViolationException exception,
            final String email
    ) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();
        if (message != null && message.contains("uk_users_email")) {
            LOGGER.warn("Registration failed due to duplicate email constraint for email={}", email);
            return new EmailAlreadyUsedException("Email is already used");
        }

        LOGGER.error("Unhandled data integrity violation during registration for email={}", email, exception);
        return exception;
    }
}
