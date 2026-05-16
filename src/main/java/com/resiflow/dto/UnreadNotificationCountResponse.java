package com.resiflow.dto;

public class UnreadNotificationCountResponse {

    private final long count;

    public UnreadNotificationCountResponse(final long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
