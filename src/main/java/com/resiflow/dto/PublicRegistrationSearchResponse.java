package com.resiflow.dto;

import java.util.List;

public class PublicRegistrationSearchResponse {

    private final Long residenceId;
    private final String residenceCode;
    private final PublicRegistrationCompositionType compositionType;
    private final List<PublicRegistrationFilterField> allowedFilters;
    private final String numeroFilter;
    private final String immeubleFilter;
    private final int totalCount;
    private final List<PublicRegistrationLogementResponse> items;

    public PublicRegistrationSearchResponse(
            final Long residenceId,
            final String residenceCode,
            final PublicRegistrationCompositionType compositionType,
            final List<PublicRegistrationFilterField> allowedFilters,
            final String numeroFilter,
            final String immeubleFilter,
            final int totalCount,
            final List<PublicRegistrationLogementResponse> items
    ) {
        this.residenceId = residenceId;
        this.residenceCode = residenceCode;
        this.compositionType = compositionType;
        this.allowedFilters = allowedFilters;
        this.numeroFilter = numeroFilter;
        this.immeubleFilter = immeubleFilter;
        this.totalCount = totalCount;
        this.items = items;
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

    public String getNumeroFilter() {
        return numeroFilter;
    }

    public String getImmeubleFilter() {
        return immeubleFilter;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<PublicRegistrationLogementResponse> getItems() {
        return items;
    }
}
