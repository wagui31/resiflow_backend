package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateAdminDepensePartagePaiementRequest {

    private Long logementId;
    private BigDecimal montant;

    public Long getLogementId() {
        return logementId;
    }

    public void setLogementId(final Long logementId) {
        this.logementId = logementId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }
}
