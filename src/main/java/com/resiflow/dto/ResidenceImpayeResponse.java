package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class ResidenceImpayeResponse {

    private final Long id;
    private final String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFinDernierPaiement;
    private final Long nombreJoursRetard;

    public ResidenceImpayeResponse(
            final Long id,
            final String email,
            final LocalDate dateFinDernierPaiement,
            final Long nombreJoursRetard
    ) {
        this.id = id;
        this.email = email;
        this.dateFinDernierPaiement = dateFinDernierPaiement;
        this.nombreJoursRetard = nombreJoursRetard;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateFinDernierPaiement() {
        return dateFinDernierPaiement;
    }

    public Long getNombreJoursRetard() {
        return nombreJoursRetard;
    }
}
