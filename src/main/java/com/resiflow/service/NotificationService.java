package com.resiflow.service;

import com.resiflow.dto.NotificationResponse;
import com.resiflow.entity.AppNotification;
import com.resiflow.entity.NotificationRecipient;
import com.resiflow.entity.NotificationType;
import com.resiflow.entity.RelatedEntityType;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.AppNotificationRepository;
import com.resiflow.repository.NotificationRecipientRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificationService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 200;

    private final AppNotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final UserRepository userRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public NotificationService(
            final AppNotificationRepository notificationRepository,
            final NotificationRecipientRepository notificationRecipientRepository,
            final UserRepository userRepository,
            final org.springframework.context.ApplicationEventPublisher eventPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationDispatch(final NotificationDispatchEvent event) {
        if (event == null || event.residenceId() == null || event.type() == null) {
            return;
        }

        List<User> recipients = resolveRecipients(event);
        if (recipients.isEmpty()) {
            return;
        }

        AppNotification notification = new AppNotification();
        notification.setResidence(toResidenceReference(event.residenceId()));
        notification.setType(event.type());
        notification.setTitle(event.title());
        notification.setBody(event.body());
        notification.setRelatedEntityType(event.relatedEntityType());
        notification.setRelatedEntityId(event.relatedEntityId());
        notification.setCreatedBy(toUserReference(event.createdByUserId()));
        AppNotification savedNotification = notificationRepository.save(notification);
        List<Long> recipientUserIds = new java.util.ArrayList<>();

        for (User recipient : recipients) {
            NotificationRecipient notificationRecipient = new NotificationRecipient();
            notificationRecipient.setNotification(savedNotification);
            notificationRecipient.setUser(recipient);
            notificationRecipient.setRead(false);
            notificationRecipient.setReadAt(null);
            notificationRecipientRepository.save(notificationRecipient);
            recipientUserIds.add(recipient.getId());
        }

        eventPublisher.publishEvent(new PushDispatchRequestedEvent(savedNotification.getId(), List.copyOf(recipientUserIds)));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            final AuthenticatedUser authenticatedUser,
            final boolean unreadOnly,
            final List<NotificationType> types,
            final Integer limit
    ) {
        Long userId = requireUserId(authenticatedUser);
        List<NotificationType> normalizedTypes = types == null ? List.of() : types;
        int resolvedLimit = resolveLimit(limit);
        return notificationRecipientRepository.findRecentForUser(
                        userId,
                        unreadOnly,
                        normalizedTypes,
                        normalizedTypes.isEmpty(),
                        PageRequest.of(0, resolvedLimit)
                ).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(final AuthenticatedUser authenticatedUser, final List<NotificationType> types) {
        Long userId = requireUserId(authenticatedUser);
        List<NotificationType> normalizedTypes = types == null ? List.of() : types;
        return notificationRecipientRepository.countUnreadForUser(userId, normalizedTypes, normalizedTypes.isEmpty());
    }

    @Transactional
    public NotificationResponse markAsRead(final Long notificationId, final AuthenticatedUser authenticatedUser) {
        Long userId = requireUserId(authenticatedUser);
        NotificationRecipient recipient = notificationRecipientRepository.findByNotification_IdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + notificationId));
        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(LocalDateTime.now());
            notificationRecipientRepository.save(recipient);
        }
        return NotificationResponse.fromEntity(recipient);
    }

    @Transactional
    public void markAllAsRead(final AuthenticatedUser authenticatedUser, final List<NotificationType> types) {
        Long userId = requireUserId(authenticatedUser);
        List<NotificationType> normalizedTypes = types == null ? List.of() : types;
        List<NotificationRecipient> recipients = notificationRecipientRepository.findUnreadForUser(
                userId,
                normalizedTypes,
                normalizedTypes.isEmpty()
        );
        if (recipients.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (NotificationRecipient recipient : recipients) {
            recipient.setRead(true);
            recipient.setReadAt(now);
        }
        notificationRecipientRepository.saveAll(recipients);
    }

    private List<User> resolveRecipients(final NotificationDispatchEvent event) {
        if (event.audience() == NotificationAudience.SINGLE_USER) {
            if (event.targetUserId() == null) {
                return List.of();
            }
            return userRepository.findById(event.targetUserId())
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(List::of)
                    .orElse(List.of());
        }

        if (event.audience() == NotificationAudience.ADMINS) {
            return userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                    event.residenceId(),
                    UserStatus.ACTIVE,
                    List.of(UserRole.ADMIN)
            );
        }

        return userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                event.residenceId(),
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
    }

    private Residence toResidenceReference(final Long residenceId) {
        Residence residence = new Residence();
        residence.setId(residenceId);
        return residence;
    }

    private User toUserReference(final Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Long requireUserId(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        return authenticatedUser.userId();
    }

    private int resolveLimit(final Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
