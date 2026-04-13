package com.resiflow.dto;

import java.math.BigDecimal;

public class ResidenceViewOverviewResponse {

    private final Long residenceId;
    private final Long totalLogements;
    private final Long activeLogements;
    private final Long inactiveLogements;
    private final Long totalResidents;
    private final Long activeResidents;
    private final Long pendingResidents;
    private final Long adminResidents;
    private final Long userResidents;
    private final BigDecimal cagnotteSolde;
    private final String cagnotteStatus;
    private final Long logementsAJour;
    private final Long logementsEnRetard;

    public ResidenceViewOverviewResponse(
            final Long residenceId,
            final Long totalLogements,
            final Long activeLogements,
            final Long inactiveLogements,
            final Long totalResidents,
            final Long activeResidents,
            final Long pendingResidents,
            final Long adminResidents,
            final Long userResidents,
            final BigDecimal cagnotteSolde,
            final String cagnotteStatus,
            final Long logementsAJour,
            final Long logementsEnRetard
    ) {
        this.residenceId = residenceId;
        this.totalLogements = totalLogements;
        this.activeLogements = activeLogements;
        this.inactiveLogements = inactiveLogements;
        this.totalResidents = totalResidents;
        this.activeResidents = activeResidents;
        this.pendingResidents = pendingResidents;
        this.adminResidents = adminResidents;
        this.userResidents = userResidents;
        this.cagnotteSolde = cagnotteSolde;
        this.cagnotteStatus = cagnotteStatus;
        this.logementsAJour = logementsAJour;
        this.logementsEnRetard = logementsEnRetard;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getTotalLogements() {
        return totalLogements;
    }

    public Long getActiveLogements() {
        return activeLogements;
    }

    public Long getInactiveLogements() {
        return inactiveLogements;
    }

    public Long getTotalResidents() {
        return totalResidents;
    }

    public Long getActiveResidents() {
        return activeResidents;
    }

    public Long getPendingResidents() {
        return pendingResidents;
    }

    public Long getAdminResidents() {
        return adminResidents;
    }

    public Long getUserResidents() {
        return userResidents;
    }

    public BigDecimal getCagnotteSolde() {
        return cagnotteSolde;
    }

    public String getCagnotteStatus() {
        return cagnotteStatus;
    }

    public Long getLogementsAJour() {
        return logementsAJour;
    }

    public Long getLogementsEnRetard() {
        return logementsEnRetard;
    }
}
