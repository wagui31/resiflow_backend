package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateAdminDepensePartagePaiementRequest {

    private Long utilisateurId;
    private BigDecimal montant;

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(final Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }
}
