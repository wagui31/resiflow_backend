package com.resiflow.dto;

import java.util.List;

public class ResidenceViewPendingLogementCardResponse {

    private final LogementResponse logement;
    private final LogementOccupancyResponse occupancy;
    private final List<ResidenceViewResidentResponse> existingResidents;
    private final List<ResidenceViewResidentResponse> pendingResidents;

    public ResidenceViewPendingLogementCardResponse(
            final LogementResponse logement,
            final LogementOccupancyResponse occupancy,
            final List<ResidenceViewResidentResponse> existingResidents,
            final List<ResidenceViewResidentResponse> pendingResidents
    ) {
        this.logement = logement;
        this.occupancy = occupancy;
        this.existingResidents = existingResidents;
        this.pendingResidents = pendingResidents;
    }

    public LogementResponse getLogement() {
        return logement;
    }

    public LogementOccupancyResponse getOccupancy() {
        return occupancy;
    }

    public List<ResidenceViewResidentResponse> getExistingResidents() {
        return existingResidents;
    }

    public List<ResidenceViewResidentResponse> getPendingResidents() {
        return pendingResidents;
    }
}
