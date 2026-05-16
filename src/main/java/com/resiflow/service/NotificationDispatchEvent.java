package com.resiflow.service;

import com.resiflow.entity.NotificationType;
import com.resiflow.entity.RelatedEntityType;

public record NotificationDispatchEvent(
        Long residenceId,
        NotificationType type,
        String title,
        String body,
        RelatedEntityType relatedEntityType,
        Long relatedEntityId,
        Long createdByUserId,
        NotificationAudience audience,
        Long targetUserId
) {
}
