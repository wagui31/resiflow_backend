package com.resiflow.dto;

public class LogementOccupancyResponse {

    private final Long logementId;
    private final Long occupiedCount;
    private final Integer maxOccupants;
    private final boolean full;

    public LogementOccupancyResponse(
            final Long logementId,
            final Long occupiedCount,
            final Integer maxOccupants,
            final boolean full
    ) {
        this.logementId = logementId;
        this.occupiedCount = occupiedCount;
        this.maxOccupants = maxOccupants;
        this.full = full;
    }

    public Long getLogementId() {
        return logementId;
    }

    public Long getOccupiedCount() {
        return occupiedCount;
    }

    public Integer getMaxOccupants() {
        return maxOccupants;
    }

    public boolean isFull() {
        return full;
    }
}
