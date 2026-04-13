package com.resiflow.dto;

import java.util.List;

public class ResidenceViewResponse {

    private final ResidenceViewOverviewResponse overview;
    private final List<ResidenceViewLogementCardResponse> logements;
    private final List<ResidenceViewPendingLogementCardResponse> pendingLogements;

    public ResidenceViewResponse(
            final ResidenceViewOverviewResponse overview,
            final List<ResidenceViewLogementCardResponse> logements,
            final List<ResidenceViewPendingLogementCardResponse> pendingLogements
    ) {
        this.overview = overview;
        this.logements = logements;
        this.pendingLogements = pendingLogements;
    }

    public ResidenceViewOverviewResponse getOverview() {
        return overview;
    }

    public List<ResidenceViewLogementCardResponse> getLogements() {
        return logements;
    }

    public List<ResidenceViewPendingLogementCardResponse> getPendingLogements() {
        return pendingLogements;
    }
}
