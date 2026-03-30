package com.resiflow.dto;

import java.time.LocalDate;

public class CreatePaiementRequest {

    private Long utilisateurId;
    private Long residenceId;
    private Integer nombreMois;
    private LocalDate dateDebut;

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(final Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public void setResidenceId(final Long residenceId) {
        this.residenceId = residenceId;
    }

    public Integer getNombreMois() {
        return nombreMois;
    }

    public void setNombreMois(final Integer nombreMois) {
        this.nombreMois = nombreMois;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(final LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
}
