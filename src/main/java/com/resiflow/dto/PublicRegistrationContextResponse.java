package com.resiflow.dto;

import java.util.List;

public class PublicRegistrationContextResponse {

    private final Long residenceId;
    private final String residenceCode;
    private final PublicRegistrationCompositionType compositionType;
    private final List<PublicRegistrationFilterField> allowedFilters;
    private final boolean hasMaison;
    private final boolean hasAppartement;
    private final int totalLogements;

    public PublicRegistrationContextResponse(
            final Long residenceId,
            final String residenceCode,
            final PublicRegistrationCompositionType compositionType,
            final List<PublicRegistrationFilterField> allowedFilters,
            final boolean hasMaison,
            final boolean hasAppartement,
            final int totalLogements
    ) {
        this.residenceId = residenceId;
        this.residenceCode = residenceCode;
        this.compositionType = compositionType;
        this.allowedFilters = allowedFilters;
        this.hasMaison = hasMaison;
        this.hasAppartement = hasAppartement;
        this.totalLogements = totalLogements;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public String getResidenceCode() {
        return residenceCode;
    }

    public PublicRegistrationCompositionType getCompositionType() {
        return compositionType;
    }

    public List<PublicRegistrationFilterField> getAllowedFilters() {
        return allowedFilters;
    }

    public boolean isHasMaison() {
        return hasMaison;
    }

    public boolean isHasAppartement() {
        return hasAppartement;
    }

    public int getTotalLogements() {
        return totalLogements;
    }
}
