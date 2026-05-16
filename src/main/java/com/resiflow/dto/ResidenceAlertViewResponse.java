package com.resiflow.dto;

import java.util.List;

public class ResidenceAlertViewResponse {

    private final Long residenceId;
    private final Long overdueLogementsCount;
    private final Long inactiveLogementsCount;
    private final List<ResidenceAlertLogementResponse> logements;

    public ResidenceAlertViewResponse(
            final Long residenceId,
            final Long overdueLogementsCount,
            final Long inactiveLogementsCount,
            final List<ResidenceAlertLogementResponse> logements
    ) {
        this.residenceId = residenceId;
        this.overdueLogementsCount = overdueLogementsCount;
        this.inactiveLogementsCount = inactiveLogementsCount;
        this.logements = logements;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getOverdueLogementsCount() {
        return overdueLogementsCount;
    }

    public Long getInactiveLogementsCount() {
        return inactiveLogementsCount;
    }

    public List<ResidenceAlertLogementResponse> getLogements() {
        return logements;
    }
}
