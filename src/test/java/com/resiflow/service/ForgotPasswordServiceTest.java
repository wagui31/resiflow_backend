package com.resiflow.service;

import com.resiflow.config.PasswordResetProperties;
import com.resiflow.dto.ForgotPasswordRequestCodeRequest;
import com.resiflow.dto.ForgotPasswordResetPasswordRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeResponse;
import com.resiflow.entity.PasswordResetRequest;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.PasswordResetRequestRepository;
import com.resiflow.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private ForgotPasswordService forgotPasswordService;

    @BeforeEach
    void setUp() {
        forgotPasswordService = new ForgotPasswordService(
                userRepository,
                passwordResetRequestRepository,
                passwordEncoder,
                new PasswordPolicyValidator(),
                emailService,
                new PasswordResetProperties(60, 30, 5, 3, 60, 7)
        );
    }

    @Test
    void requestCodeCreatesRequestAndSendsEmailForActiveUser() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(user));
        when(passwordResetRequestRepository.findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.empty());
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordRequestCodeRequest request = new ForgotPasswordRequestCodeRequest();
        request.setEmail("resident@example.com");

        forgotPasswordService.requestCode(request);

        ArgumentCaptor<PasswordResetRequest> captor = ArgumentCaptor.forClass(PasswordResetRequest.class);
        verify(passwordResetRequestRepository).save(captor.capture());
        PasswordResetRequest savedRequest = captor.getValue();
        assertThat(savedRequest.getUser().getId()).isEqualTo(7L);
        assertThat(savedRequest.getCodeHash()).isNotBlank();
        assertThat(savedRequest.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(59));
        verify(emailService).sendToUser(eq("resident@example.com"), any(), any());
    }

    @Test
    void requestCodeDoesNotSendEmailWhenUserIsMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ForgotPasswordRequestCodeRequest request = new ForgotPasswordRequestCodeRequest();
        request.setEmail("missing@example.com");

        forgotPasswordService.requestCode(request);

        verify(passwordResetRequestRepository, never()).save(any());
        verify(emailService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void verifyCodeReturnsResetSessionTokenWhenCodeMatches() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        PasswordResetRequest storedRequest = buildResetRequest(user);
        storedRequest.setCodeHash(hash("123456"));
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(user));
        when(passwordResetRequestRepository.findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(storedRequest));
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordVerifyCodeRequest request = new ForgotPasswordVerifyCodeRequest();
        request.setEmail("resident@example.com");
        request.setCode("123456");

        ForgotPasswordVerifyCodeResponse response = forgotPasswordService.verifyCode(request);

        assertThat(response.getResetSessionToken()).isNotBlank();
        assertThat(response.getResetSessionExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(storedRequest.getCodeHash()).isNull();
        assertThat(storedRequest.getResetSessionHash()).isNotBlank();
    }

    @Test
    void verifyCodeRejectsExpiredCode() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        PasswordResetRequest storedRequest = buildResetRequest(user);
        storedRequest.setCodeHash(hash("123456"));
        storedRequest.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(user));
        when(passwordResetRequestRepository.findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(storedRequest));
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordVerifyCodeRequest request = new ForgotPasswordVerifyCodeRequest();
        request.setEmail("resident@example.com");
        request.setCode("123456");

        assertThatThrownBy(() -> forgotPasswordService.verifyCode(request))
                .isInstanceOf(PasswordResetCodeExpiredException.class)
                .hasMessage("Reset code has expired");
        assertThat(storedRequest.getInvalidatedAt()).isNotNull();
    }

    @Test
    void verifyCodeInvalidatesRequestAfterMaxAttempts() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        PasswordResetRequest storedRequest = buildResetRequest(user);
        storedRequest.setCodeHash(hash("123456"));
        storedRequest.setAttemptCount(4);
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(user));
        when(passwordResetRequestRepository.findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(storedRequest));
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordVerifyCodeRequest request = new ForgotPasswordVerifyCodeRequest();
        request.setEmail("resident@example.com");
        request.setCode("000000");

        assertThatThrownBy(() -> forgotPasswordService.verifyCode(request))
                .isInstanceOf(PasswordResetRateLimitException.class)
                .hasMessage("Too many invalid reset code attempts");
        assertThat(storedRequest.getInvalidatedAt()).isNotNull();
    }

    @Test
    void resetPasswordUpdatesUserPasswordAndMarksRequestUsed() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("CurrentPass1!"));
        PasswordResetRequest storedRequest = buildResetRequest(user);
        storedRequest.setCodeHash(null);
        storedRequest.setResetSessionHash(hash("reset-session-token"));
        storedRequest.setCodeVerifiedAt(LocalDateTime.now().minusMinutes(1));
        storedRequest.setResetSessionExpiresAt(LocalDateTime.now().plusMinutes(30));
        when(passwordResetRequestRepository.findFirstByResetSessionHashAndUsedAtIsNullAndInvalidatedAtIsNull(hash("reset-session-token")))
                .thenReturn(Optional.of(storedRequest));
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordResetPasswordRequest request = new ForgotPasswordResetPasswordRequest();
        request.setResetSessionToken("reset-session-token");
        request.setNewPassword("NewStrong1!");
        request.setConfirmPassword("NewStrong1!");

        forgotPasswordService.resetPassword(request);

        assertThat(passwordEncoder.matches("NewStrong1!", user.getPassword())).isTrue();
        assertThat(storedRequest.getUsedAt()).isNotNull();
        assertThat(storedRequest.getResetSessionHash()).isNull();
    }

    @Test
    void resetPasswordRejectsExpiredSession() {
        User user = buildUser(7L, "resident@example.com", UserStatus.ACTIVE);
        PasswordResetRequest storedRequest = buildResetRequest(user);
        storedRequest.setCodeHash(null);
        storedRequest.setResetSessionHash(hash("reset-session-token"));
        storedRequest.setResetSessionExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(passwordResetRequestRepository.findFirstByResetSessionHashAndUsedAtIsNullAndInvalidatedAtIsNull(hash("reset-session-token")))
                .thenReturn(Optional.of(storedRequest));
        when(passwordResetRequestRepository.save(any(PasswordResetRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordResetPasswordRequest request = new ForgotPasswordResetPasswordRequest();
        request.setResetSessionToken("reset-session-token");
        request.setNewPassword("NewStrong1!");
        request.setConfirmPassword("NewStrong1!");

        assertThatThrownBy(() -> forgotPasswordService.resetPassword(request))
                .isInstanceOf(PasswordResetSessionInvalidException.class)
                .hasMessage("Reset session has expired");
    }

    private User buildUser(final Long id, final String email, final UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(UserRole.USER);
        user.setStatus(status);
        return user;
    }

    private PasswordResetRequest buildResetRequest(final User user) {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setId(11L);
        request.setUser(user);
        request.setEmailSnapshot(user.getEmail());
        request.setExpiresAt(LocalDateTime.now().plusHours(1));
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        request.setLastSentAt(LocalDateTime.now());
        return request;
    }

    private String hash(final String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
