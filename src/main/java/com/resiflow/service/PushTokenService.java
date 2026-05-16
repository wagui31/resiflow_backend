package com.resiflow.service;

import com.resiflow.dto.PushTokenDeactivateRequest;
import com.resiflow.dto.PushTokenUpsertRequest;
import com.resiflow.entity.PushTokenStatus;
import com.resiflow.entity.User;
import com.resiflow.entity.UserPushToken;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserPushTokenRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushTokenService.class);

    private final UserPushTokenRepository userPushTokenRepository;
    private final UserRepository userRepository;

    public PushTokenService(
            final UserPushTokenRepository userPushTokenRepository,
            final UserRepository userRepository
    ) {
        this.userPushTokenRepository = userPushTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserPushToken registerCurrentUserToken(
            final AuthenticatedUser authenticatedUser,
            final PushTokenUpsertRequest request
    ) {
        validateUpsertRequest(request);
        User user = requireActiveUser(authenticatedUser);
        String normalizedToken = normalizeRequired(request.getToken(), "Push token must not be blank");
        String normalizedInstallationId = normalizeRequired(
                request.getInstallationId(),
                "Installation ID must not be blank"
        );
        LocalDateTime now = LocalDateTime.now();

        List<UserPushToken> installationTokens = userPushTokenRepository.findAllByInstallationId(normalizedInstallationId);
        UserPushToken token = userPushTokenRepository.findByToken(normalizedToken).orElse(null);

        for (UserPushToken existing : installationTokens) {
            if (token != null && Objects.equals(existing.getId(), token.getId())) {
                continue;
            }
            markInactive(existing, PushTokenStatus.REPLACED, now);
        }

        if (token == null) {
            token = new UserPushToken();
            token.setToken(normalizedToken);
        }

        token.setUser(user);
        token.setPlatform(request.getPlatform());
        token.setInstallationId(normalizedInstallationId);
        token.setDeviceName(normalizeOptional(request.getDeviceName()));
        token.setAppVersion(normalizeOptional(request.getAppVersion()));
        token.setStatus(PushTokenStatus.ACTIVE);
        token.setInvalidatedAt(null);
        token.setLastSeenAt(now);

        UserPushToken savedToken = userPushTokenRepository.save(token);
        LOGGER.info(
                "Registered push token id={} for userId={} installationId={} platform={} status={}",
                savedToken.getId(),
                user.getId(),
                savedToken.getInstallationId(),
                savedToken.getPlatform(),
                savedToken.getStatus()
        );
        return savedToken;
    }

    @Transactional
    public void logoutCurrentUserToken(
            final AuthenticatedUser authenticatedUser,
            final PushTokenDeactivateRequest request
    ) {
        User user = requireUser(authenticatedUser);
        List<UserPushToken> matchingTokens = findCurrentUserMatchingTokens(user.getId(), request);
        LocalDateTime now = LocalDateTime.now();
        for (UserPushToken token : matchingTokens) {
            markInactive(token, PushTokenStatus.LOGGED_OUT, now);
        }
        if (!matchingTokens.isEmpty()) {
            userPushTokenRepository.saveAll(matchingTokens);
        }
    }

    @Transactional
    public void markAllTokensArchived(final Long userId) {
        updateAllUserTokensStatus(userId, PushTokenStatus.USER_ARCHIVED);
    }

    @Transactional
    public void markAllTokensDeleted(final Long userId) {
        List<UserPushToken> tokens = userPushTokenRepository.findAllByUser_Id(userId);
        if (tokens.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserPushToken token : tokens) {
            markInactive(token, PushTokenStatus.USER_DELETED, now);
        }
        userPushTokenRepository.saveAll(tokens);
        userPushTokenRepository.deleteAllByUser_Id(userId);
        LOGGER.info("Deleted {} push token(s) for deleted userId={}", tokens.size(), userId);
    }

    @Transactional
    public void markTokensInvalid(final Collection<Long> tokenIds) {
        if (tokenIds == null || tokenIds.isEmpty()) {
            return;
        }
        List<UserPushToken> tokens = userPushTokenRepository.findAllById(tokenIds);
        if (tokens.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserPushToken token : tokens) {
            markInactive(token, PushTokenStatus.INVALID, now);
        }
        userPushTokenRepository.saveAll(tokens);
    }

    @Transactional(readOnly = true)
    public List<UserPushToken> getActiveTokensForUsers(final Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userPushTokenRepository.findAllByUser_IdInAndStatus(userIds, PushTokenStatus.ACTIVE);
    }

    private void updateAllUserTokensStatus(final Long userId, final PushTokenStatus status) {
        List<UserPushToken> tokens = userPushTokenRepository.findAllByUser_Id(userId);
        if (tokens.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserPushToken token : tokens) {
            markInactive(token, status, now);
        }
        userPushTokenRepository.saveAll(tokens);
        LOGGER.info("Updated {} push token(s) to status={} for userId={}", tokens.size(), status, userId);
    }

    private List<UserPushToken> findCurrentUserMatchingTokens(
            final Long userId,
            final PushTokenDeactivateRequest request
    ) {
        validateDeactivateRequest(request);
        String normalizedToken = normalizeOptional(request.getToken());
        String normalizedInstallationId = normalizeOptional(request.getInstallationId());
        List<UserPushToken> matches = new ArrayList<>();

        if (normalizedToken != null) {
            userPushTokenRepository.findByUser_IdAndToken(userId, normalizedToken).ifPresent(matches::add);
        }
        if (normalizedInstallationId != null) {
            for (UserPushToken token : userPushTokenRepository.findAllByUser_IdAndInstallationId(userId, normalizedInstallationId)) {
                if (matches.stream().noneMatch(existing -> existing.getId().equals(token.getId()))) {
                    matches.add(token);
                }
            }
        }
        return matches;
    }

    private User requireUser(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        return userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authenticatedUser.userId()));
    }

    private User requireActiveUser(final AuthenticatedUser authenticatedUser) {
        User user = requireUser(authenticatedUser);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only active users can register push tokens");
        }
        return user;
    }

    private void validateUpsertRequest(final PushTokenUpsertRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Push token request must not be null");
        }
        if (request.getPlatform() == null) {
            throw new IllegalArgumentException("Push token platform must not be null");
        }
        normalizeRequired(request.getToken(), "Push token must not be blank");
        normalizeRequired(request.getInstallationId(), "Installation ID must not be blank");
    }

    private void validateDeactivateRequest(final PushTokenDeactivateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Push token deactivate request must not be null");
        }
        if (normalizeOptional(request.getToken()) == null && normalizeOptional(request.getInstallationId()) == null) {
            throw new IllegalArgumentException("Push token or installation ID must be provided");
        }
    }

    private String normalizeRequired(final String value, final String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeOptional(final String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void markInactive(
            final UserPushToken token,
            final PushTokenStatus targetStatus,
            final LocalDateTime now
    ) {
        token.setStatus(targetStatus);
        token.setInvalidatedAt(now);
        token.setLastSeenAt(now);
    }
}
