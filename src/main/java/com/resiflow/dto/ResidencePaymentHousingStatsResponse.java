package com.resiflow.dto;

public class ResidencePaymentHousingStatsResponse {

    private final Long residenceId;
    private final Long totalLogementsActifs;
    private final Long logementsAJour;
    private final Long logementsEnRetard;

    public ResidencePaymentHousingStatsResponse(
            final Long residenceId,
            final Long totalLogementsActifs,
            final Long logementsAJour,
            final Long logementsEnRetard
    ) {
        this.residenceId = residenceId;
        this.totalLogementsActifs = totalLogementsActifs;
        this.logementsAJour = logementsAJour;
        this.logementsEnRetard = logementsEnRetard;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getTotalLogementsActifs() {
        return totalLogementsActifs;
    }

    public Long getLogementsAJour() {
        return logementsAJour;
    }

    public Long getLogementsEnRetard() {
        return logementsEnRetard;
    }
}
