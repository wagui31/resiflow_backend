package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateDepenseRequest {

    private Long residenceId;
    private Long categorieId;
    private BigDecimal montant;
    private String description;

    public Long getResidenceId() {
        return residenceId;
    }

    public void setResidenceId(final Long residenceId) {
        this.residenceId = residenceId;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(final Long categorieId) {
        this.categorieId = categorieId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
