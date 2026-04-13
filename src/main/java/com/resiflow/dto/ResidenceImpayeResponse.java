package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class ResidenceImpayeResponse {

    private final Long logementId;
    private final LogementSummaryResponse logement;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFinDernierPaiement;
    private final Long nombreJoursRetard;

    public ResidenceImpayeResponse(
            final Long logementId,
            final LogementSummaryResponse logement,
            final LocalDate dateFinDernierPaiement,
            final Long nombreJoursRetard
    ) {
        this.logementId = logementId;
        this.logement = logement;
        this.dateFinDernierPaiement = dateFinDernierPaiement;
        this.nombreJoursRetard = nombreJoursRetard;
    }

    public Long getLogementId() {
        return logementId;
    }

    public LogementSummaryResponse getLogement() {
        return logement;
    }

    public LocalDate getDateFinDernierPaiement() {
        return dateFinDernierPaiement;
    }

    public Long getNombreJoursRetard() {
        return nombreJoursRetard;
    }
}
