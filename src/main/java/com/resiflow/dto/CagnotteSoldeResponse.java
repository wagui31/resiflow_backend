package com.resiflow.dto;

import java.math.BigDecimal;

public class CagnotteSoldeResponse {

    private final Long residenceId;
    private final BigDecimal solde;

    public CagnotteSoldeResponse(final Long residenceId, final BigDecimal solde) {
        this.residenceId = residenceId;
        this.solde = solde;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public BigDecimal getSolde() {
        return solde;
    }
}
