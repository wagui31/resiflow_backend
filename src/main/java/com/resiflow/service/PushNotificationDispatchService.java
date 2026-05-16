package com.resiflow.service;

import com.resiflow.entity.AppNotification;
import com.resiflow.entity.UserPushToken;
import com.resiflow.repository.AppNotificationRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class PushNotificationDispatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushNotificationDispatchService.class);

    private final AppNotificationRepository appNotificationRepository;
    private final PushTokenService pushTokenService;
    private final PushGateway pushGateway;

    public PushNotificationDispatchService(
            final AppNotificationRepository appNotificationRepository,
            final PushTokenService pushTokenService,
            final PushGateway pushGateway
    ) {
        this.appNotificationRepository = appNotificationRepository;
        this.pushTokenService = pushTokenService;
        this.pushGateway = pushGateway;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePushDispatchRequested(final PushDispatchRequestedEvent event) {
        if (event == null || event.notificationId() == null || event.recipientUserIds() == null || event.recipientUserIds().isEmpty()) {
            return;
        }

        AppNotification notification = appNotificationRepository.findById(event.notificationId())
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + event.notificationId()));
        List<UserPushToken> tokens = pushTokenService.getActiveTokensForUsers(event.recipientUserIds());
        if (tokens.isEmpty()) {
            LOGGER.info(
                    "No active push token found for notificationId={} recipientUserCount={}",
                    event.notificationId(),
                    event.recipientUserIds().size()
            );
            return;
        }

        PushMessage pushMessage = new PushMessage(
                notification.getTitle(),
                notification.getBody(),
                buildDataPayload(notification)
        );

        try {
            PushSendResult result = pushGateway.send(pushMessage, tokens);
            pushTokenService.markTokensInvalid(result.invalidTokenIds());
            LOGGER.info(
                    "Push dispatch processed for notificationId={} attempted={} accepted={} invalidated={}",
                    event.notificationId(),
                    result.attemptedCount(),
                    result.acceptedCount(),
                    result.invalidTokenIds().size()
            );
        } catch (RuntimeException exception) {
            LOGGER.error("Push dispatch failed for notificationId={}", event.notificationId(), exception);
        }
    }

    private Map<String, String> buildDataPayload(final AppNotification notification) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("notificationId", String.valueOf(notification.getId()));
        data.put("type", notification.getType().name());
        if (notification.getRelatedEntityType() != null) {
            data.put("relatedEntityType", notification.getRelatedEntityType().name());
        }
        if (notification.getRelatedEntityId() != null) {
            data.put("relatedEntityId", String.valueOf(notification.getRelatedEntityId()));
        }
        if (notification.getResidence() != null && notification.getResidence().getId() != null) {
            data.put("residenceId", String.valueOf(notification.getResidence().getId()));
        }
        return data;
    }
}
