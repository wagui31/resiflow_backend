package com.resiflow.dto;

import com.resiflow.entity.UserPushToken;
import java.time.LocalDateTime;

public record PushTokenResponse(
        Long id,
        String token,
        String platform,
        String installationId,
        String status,
        LocalDateTime lastSeenAt
) {

    public static PushTokenResponse fromEntity(final UserPushToken token) {
        return new PushTokenResponse(
                token.getId(),
                token.getToken(),
                token.getPlatform().name(),
                token.getInstallationId(),
                token.getStatus().name(),
                token.getLastSeenAt()
        );
    }
}
