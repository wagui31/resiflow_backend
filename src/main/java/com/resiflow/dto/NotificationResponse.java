package com.resiflow.dto;

import com.resiflow.entity.NotificationRecipient;
import com.resiflow.entity.NotificationType;
import com.resiflow.entity.RelatedEntityType;
import java.time.LocalDateTime;

public class NotificationResponse {

    private final Long id;
    private final Long residenceId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final RelatedEntityType relatedEntityType;
    private final Long relatedEntityId;
    private final LocalDateTime createdAt;
    private final boolean read;
    private final LocalDateTime readAt;

    public NotificationResponse(
            final Long id,
            final Long residenceId,
            final NotificationType type,
            final String title,
            final String body,
            final RelatedEntityType relatedEntityType,
            final Long relatedEntityId,
            final LocalDateTime createdAt,
            final boolean read,
            final LocalDateTime readAt
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.createdAt = createdAt;
        this.read = read;
        this.readAt = readAt;
    }

    public static NotificationResponse fromEntity(final NotificationRecipient recipient) {
        return new NotificationResponse(
                recipient.getNotification().getId(),
                recipient.getNotification().getResidence().getId(),
                recipient.getNotification().getType(),
                recipient.getNotification().getTitle(),
                recipient.getNotification().getBody(),
                recipient.getNotification().getRelatedEntityType(),
                recipient.getNotification().getRelatedEntityId(),
                recipient.getNotification().getCreatedAt(),
                recipient.isRead(),
                recipient.getReadAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public RelatedEntityType getRelatedEntityType() {
        return relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}
