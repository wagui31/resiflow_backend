package com.resiflow.service;

import java.util.List;

public record PushSendResult(
        int attemptedCount,
        int acceptedCount,
        List<Long> invalidTokenIds
) {

    public static PushSendResult skipped(final int attemptedCount) {
        return new PushSendResult(attemptedCount, 0, List.of());
    }
}
