package com.resiflow.dto;

import com.resiflow.entity.TypeDepense;
import java.math.BigDecimal;

public class CreateDepenseRequest {

    private Long residenceId;
    private Long categorieId;
    private BigDecimal montant;
    private TypeDepense typeDepense;
    private BigDecimal montantParPersonne;
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

    public TypeDepense getTypeDepense() {
        return typeDepense;
    }

    public void setTypeDepense(final TypeDepense typeDepense) {
        this.typeDepense = typeDepense;
    }

    public BigDecimal getMontantParPersonne() {
        return montantParPersonne;
    }

    public void setMontantParPersonne(final BigDecimal montantParPersonne) {
        this.montantParPersonne = montantParPersonne;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
