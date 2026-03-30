package com.resiflow.dto;

import java.math.BigDecimal;

public class EvolutionCagnotteResponse {

    private final String mois;
    private final BigDecimal solde;

    public EvolutionCagnotteResponse(final String mois, final BigDecimal solde) {
        this.mois = mois;
        this.solde = solde;
    }

    public String getMois() {
        return mois;
    }

    public BigDecimal getSolde() {
        return solde;
    }
}
