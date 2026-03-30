package com.resiflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateVoteRequest {

    private Long residenceId;
    private String titre;
    private String description;
    private BigDecimal montantEstime;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public Long getResidenceId() {
        return residenceId;
    }

    public void setResidenceId(final Long residenceId) {
        this.residenceId = residenceId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(final String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public BigDecimal getMontantEstime() {
        return montantEstime;
    }

    public void setMontantEstime(final BigDecimal montantEstime) {
        this.montantEstime = montantEstime;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(final LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(final LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}
