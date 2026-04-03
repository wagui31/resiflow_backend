package com.resiflow.dto;

import java.time.LocalDate;

public class CreateMyPaiementRequest {

    private Integer nombreMois;
    private LocalDate dateDebut;

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
