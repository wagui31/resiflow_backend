package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateDepensePartagePaiementRequest {

    private BigDecimal montant;

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }
}
