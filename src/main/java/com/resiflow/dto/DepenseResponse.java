package com.resiflow.dto;

import com.resiflow.entity.Depense;
import com.resiflow.entity.StatutDepense;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepenseResponse {

    private final Long id;
    private final Long residenceId;
    private final Long categorieId;
    private final String categorieNom;
    private final BigDecimal montant;
    private final String description;
    private final StatutDepense statut;
    private final Long creeParId;
    private final LocalDateTime dateCreation;
    private final Long valideParId;
    private final LocalDateTime dateValidation;

    public DepenseResponse(
            final Long id,
            final Long residenceId,
            final Long categorieId,
            final String categorieNom,
            final BigDecimal montant,
            final String description,
            final StatutDepense statut,
            final Long creeParId,
            final LocalDateTime dateCreation,
            final Long valideParId,
            final LocalDateTime dateValidation
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.categorieId = categorieId;
        this.categorieNom = categorieNom;
        this.montant = montant;
        this.description = description;
        this.statut = statut;
        this.creeParId = creeParId;
        this.dateCreation = dateCreation;
        this.valideParId = valideParId;
        this.dateValidation = dateValidation;
    }

    public static DepenseResponse fromEntity(final Depense depense) {
        return new DepenseResponse(
                depense.getId(),
                depense.getResidence().getId(),
                depense.getCategorie() == null ? null : depense.getCategorie().getId(),
                depense.getCategorie() == null ? null : depense.getCategorie().getNom(),
                depense.getMontant(),
                depense.getDescription(),
                depense.getStatut(),
                depense.getCreePar().getId(),
                depense.getDateCreation(),
                depense.getValidePar() == null ? null : depense.getValidePar().getId(),
                depense.getDateValidation()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getDescription() {
        return description;
    }

    public StatutDepense getStatut() {
        return statut;
    }

    public Long getCreeParId() {
        return creeParId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Long getValideParId() {
        return valideParId;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }
}
