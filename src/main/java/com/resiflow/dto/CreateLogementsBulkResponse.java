package com.resiflow.dto;

import java.util.List;

public class CreateLogementsBulkResponse {

    private final int createdCount;
    private final List<LogementResponse> logements;

    public CreateLogementsBulkResponse(final int createdCount, final List<LogementResponse> logements) {
        this.createdCount = createdCount;
        this.logements = logements;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public List<LogementResponse> getLogements() {
        return logements;
    }
}
