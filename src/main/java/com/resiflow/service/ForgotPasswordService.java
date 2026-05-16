package com.resiflow.service;

import com.resiflow.config.PasswordResetProperties;
import com.resiflow.dto.ForgotPasswordRequestCodeRequest;
import com.resiflow.dto.ForgotPasswordRequestCodeResponse;
import com.resiflow.dto.ForgotPasswordResetPasswordRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeResponse;
import com.resiflow.entity.PasswordResetRequest;
import com.resiflow.entity.User;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.PasswordResetRequestRepository;
import com.resiflow.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForgotPasswordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordService.class);
    private static final String GENERIC_MESSAGE =
            "Si un compte existe pour cet email, un code de reinitialisation a ete envoye.";
    private static final int CODE_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final EmailService emailService;
    private final PasswordResetProperties properties;
    private final SecureRandom secureRandom;

    @Autowired
    public ForgotPasswordService(
            final UserRepository userRepository,
            final PasswordResetRequestRepository passwordResetRequestRepository,
            final PasswordEncoder passwordEncoder,
            final PasswordPolicyValidator passwordPolicyValidator,
            final EmailService emailService,
            final PasswordResetProperties properties
    ) {
        this(userRepository, passwordResetRequestRepository, passwordEncoder, passwordPolicyValidator, emailService, properties, new SecureRandom());
    }

    ForgotPasswordService(
            final UserRepository userRepository,
            final PasswordResetRequestRepository passwordResetRequestRepository,
            final PasswordEncoder passwordEncoder,
            final PasswordPolicyValidator passwordPolicyValidator,
            final EmailService emailService,
            final PasswordResetProperties properties,
            final SecureRandom secureRandom
    ) {
        this.userRepository = userRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.emailService = emailService;
        this.properties = properties;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public ForgotPasswordRequestCodeResponse requestCode(final ForgotPasswordRequestCodeRequest request) {
        String email = validateAndNormalizeEmailRequest(request);
        LOGGER.info("Password reset code requested for email={}", maskEmail(email));

        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            LOGGER.info("Password reset request ignored because user was not found for email={}", maskEmail(email));
            return new ForgotPasswordRequestCodeResponse(GENERIC_MESSAGE);
        }

        User user = maybeUser.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            LOGGER.info(
                    "Password reset request ignored because account is not eligible userId={} email={} status={}",
                    user.getId(),
                    maskEmail(email),
                    user.getStatus()
            );
            return new ForgotPasswordRequestCodeResponse(GENERIC_MESSAGE);
        }

        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (resetRequest != null && resetRequest.getExpiresAt().isBefore(now)) {
            invalidateRequest(resetRequest, now);
            resetRequest = null;
        }

        if (resetRequest != null) {
            if (resetRequest.getLastSentAt().plusSeconds(properties.resendCooldownSeconds()).isAfter(now)) {
                LOGGER.info(
                        "Password reset request throttled by cooldown userId={} email={} requestId={}",
                        user.getId(),
                        maskEmail(email),
                        resetRequest.getId()
                );
                return new ForgotPasswordRequestCodeResponse(GENERIC_MESSAGE);
            }
            if (resetRequest.getResendCount() >= properties.maxResends()) {
                LOGGER.info(
                        "Password reset request throttled by max resends userId={} email={} requestId={} resendCount={}",
                        user.getId(),
                        maskEmail(email),
                        resetRequest.getId(),
                        resetRequest.getResendCount()
                );
                return new ForgotPasswordRequestCodeResponse(GENERIC_MESSAGE);
            }
        }

        String code = generateNumericCode();
        if (resetRequest == null) {
            resetRequest = new PasswordResetRequest();
            resetRequest.setUser(user);
            resetRequest.setEmailSnapshot(email);
            resetRequest.setAttemptCount(0);
            resetRequest.setResendCount(0);
            resetRequest.setCreatedAt(now);
        } else {
            resetRequest.setResendCount(resetRequest.getResendCount() + 1);
            resetRequest.setAttemptCount(0);
            resetRequest.setInvalidatedAt(null);
        }
        resetRequest.setCodeHash(hashSensitiveValue(code));
        resetRequest.setResetSessionHash(null);
        resetRequest.setResetSessionExpiresAt(null);
        resetRequest.setCodeVerifiedAt(null);
        resetRequest.setUsedAt(null);
        resetRequest.setLastAttemptAt(null);
        resetRequest.setLastSentAt(now);
        resetRequest.setExpiresAt(now.plusMinutes(properties.codeExpirationMinutes()));
        resetRequest.setUpdatedAt(now);
        PasswordResetRequest savedRequest = passwordResetRequestRepository.save(resetRequest);

        emailService.sendToUser(
                email,
                "Code de reinitialisation ResiFlow",
                buildResetCodeEmailBody(code)
        );
        LOGGER.info(
                "Password reset code issued userId={} email={} requestId={} expiresAt={}",
                user.getId(),
                maskEmail(email),
                savedRequest.getId(),
                savedRequest.getExpiresAt()
        );

        return new ForgotPasswordRequestCodeResponse(GENERIC_MESSAGE);
    }

    @Transactional
    public ForgotPasswordVerifyCodeResponse verifyCode(final ForgotPasswordVerifyCodeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Forgot password verify code request must not be null");
        }
        String email = normalizeRequiredValue(request.getEmail(), "Email must not be blank");
        String code = normalizeRequiredValue(request.getCode(), "Code must not be blank");
        validateCodeFormat(code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new PasswordResetCodeInvalidException("Reset code is invalid"));
        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new PasswordResetCodeInvalidException("Reset code is invalid"));

        LocalDateTime now = LocalDateTime.now();
        if (resetRequest.getCodeHash() == null) {
            LOGGER.warn(
                    "Password reset code verification rejected because no active code remains requestId={} userId={}",
                    resetRequest.getId(),
                    user.getId()
            );
            throw new PasswordResetCodeInvalidException("Reset code is invalid");
        }
        if (resetRequest.getExpiresAt().isBefore(now)) {
            invalidateRequest(resetRequest, now);
            LOGGER.warn(
                    "Password reset code verification failed because code expired requestId={} userId={}",
                    resetRequest.getId(),
                    user.getId()
            );
            throw new PasswordResetCodeExpiredException("Reset code has expired");
        }
        if (resetRequest.getAttemptCount() >= properties.maxAttempts()) {
            invalidateRequest(resetRequest, now);
            LOGGER.warn(
                    "Password reset code verification blocked because max attempts reached requestId={} userId={} attemptCount={}",
                    resetRequest.getId(),
                    user.getId(),
                    resetRequest.getAttemptCount()
            );
            throw new PasswordResetRateLimitException("Too many invalid reset code attempts");
        }

        if (!hashSensitiveValue(code).equals(resetRequest.getCodeHash())) {
            int nextAttemptCount = resetRequest.getAttemptCount() + 1;
            resetRequest.setAttemptCount(nextAttemptCount);
            resetRequest.setLastAttemptAt(now);
            if (nextAttemptCount >= properties.maxAttempts()) {
                invalidateRequest(resetRequest, now);
                LOGGER.warn(
                        "Password reset code invalidated after max attempts requestId={} userId={} attemptCount={}",
                        resetRequest.getId(),
                        user.getId(),
                        nextAttemptCount
                );
                throw new PasswordResetRateLimitException("Too many invalid reset code attempts");
            }
            passwordResetRequestRepository.save(resetRequest);
            LOGGER.warn(
                    "Password reset code verification failed because code did not match requestId={} userId={} attemptCount={}",
                    resetRequest.getId(),
                    user.getId(),
                    nextAttemptCount
            );
            throw new PasswordResetCodeInvalidException("Reset code is invalid");
        }

        String resetSessionToken = generateSessionToken();
        resetRequest.setCodeHash(null);
        resetRequest.setCodeVerifiedAt(now);
        resetRequest.setResetSessionHash(hashSensitiveValue(resetSessionToken));
        resetRequest.setResetSessionExpiresAt(now.plusMinutes(properties.resetSessionExpirationMinutes()));
        resetRequest.setLastAttemptAt(now);
        passwordResetRequestRepository.save(resetRequest);

        LOGGER.info(
                "Password reset code verified requestId={} userId={} resetSessionExpiresAt={}",
                resetRequest.getId(),
                user.getId(),
                resetRequest.getResetSessionExpiresAt()
        );
        return new ForgotPasswordVerifyCodeResponse(resetSessionToken, resetRequest.getResetSessionExpiresAt());
    }

    @Transactional
    public void resetPassword(final ForgotPasswordResetPasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Forgot password reset request must not be null");
        }
        String resetSessionToken = normalizeRequiredValue(
                request.getResetSessionToken(),
                "Reset session token must not be blank"
        );
        passwordPolicyValidator.validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findFirstByResetSessionHashAndUsedAtIsNullAndInvalidatedAtIsNull(hashSensitiveValue(resetSessionToken))
                .orElseThrow(() -> new PasswordResetSessionInvalidException("Reset session is invalid"));

        LocalDateTime now = LocalDateTime.now();
        if (resetRequest.getResetSessionExpiresAt() == null || resetRequest.getResetSessionExpiresAt().isBefore(now)) {
            invalidateRequest(resetRequest, now);
            LOGGER.warn(
                    "Password reset failed because session expired requestId={} userId={}",
                    resetRequest.getId(),
                    resetRequest.getUser().getId()
            );
            throw new PasswordResetSessionInvalidException("Reset session has expired");
        }

        User user = userRepository.findByIdForUpdate(resetRequest.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User linked to password reset request was not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            invalidateRequest(resetRequest, now);
            LOGGER.warn(
                    "Password reset failed because account is no longer eligible requestId={} userId={} status={}",
                    resetRequest.getId(),
                    user.getId(),
                    user.getStatus()
            );
            throw new PasswordResetSessionInvalidException("Reset session is invalid");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        resetRequest.setUsedAt(now);
        resetRequest.setResetSessionHash(null);
        resetRequest.setResetSessionExpiresAt(null);
        passwordResetRequestRepository.save(resetRequest);
        LOGGER.info("Password reset completed requestId={} userId={}", resetRequest.getId(), user.getId());
    }

    private void validateCodeFormat(final String code) {
        if (code.length() != CODE_LENGTH || !code.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Reset code must contain exactly 6 digits");
        }
    }

    private String validateAndNormalizeEmailRequest(final ForgotPasswordRequestCodeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Forgot password request must not be null");
        }
        return normalizeRequiredValue(request.getEmail(), "Email must not be blank");
    }

    private String normalizeRequiredValue(final String value, final String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void invalidateRequest(final PasswordResetRequest resetRequest, final LocalDateTime now) {
        resetRequest.setInvalidatedAt(now);
        resetRequest.setResetSessionHash(null);
        resetRequest.setResetSessionExpiresAt(null);
        passwordResetRequestRepository.save(resetRequest);
    }

    private String generateNumericCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private String generateSessionToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashSensitiveValue(final String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private String buildResetCodeEmailBody(final String code) {
        return """
                Bonjour,

                Voici votre code de reinitialisation ResiFlow : %s

                Ce code est valable pendant %d heure(s).
                Ne partagez jamais ce code.

                Si vous n'etes pas a l'origine de cette demande, vous pouvez ignorer cet email.
                """.formatted(code, properties.codeExpirationMinutes() / 60);
    }

    private String maskEmail(final String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
