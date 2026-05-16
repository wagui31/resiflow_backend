package com.resiflow.service;

import java.util.List;

public record PushDispatchRequestedEvent(
        Long notificationId,
        List<Long> recipientUserIds
) {
}
