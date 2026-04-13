package com.resiflow.dto;

import java.time.LocalDate;

public class CreatePaiementRequest {

    private Long logementId;
    private Long residenceId;
    private Integer nombreMois;
    private LocalDate dateDebut;

    public Long getLogementId() {
        return logementId;
    }

    public void setLogementId(final Long logementId) {
        this.logementId = logementId;
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
