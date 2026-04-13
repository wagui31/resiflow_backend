package com.resiflow.dto;

import com.resiflow.entity.Logement;
import com.resiflow.entity.TypeLogement;

public class LogementSummaryResponse {

    private final Long logementId;
    private final String numero;
    private final String immeuble;
    private final String codeInterne;
    private final TypeLogement typeLogement;
    private final Boolean active;

    public LogementSummaryResponse(
            final Long logementId,
            final String numero,
            final String immeuble,
            final String codeInterne,
            final TypeLogement typeLogement,
            final Boolean active
    ) {
        this.logementId = logementId;
        this.numero = numero;
        this.immeuble = immeuble;
        this.codeInterne = codeInterne;
        this.typeLogement = typeLogement;
        this.active = active;
    }

    public static LogementSummaryResponse fromEntity(final Logement logement) {
        if (logement == null) {
            return null;
        }
        return new LogementSummaryResponse(
                logement.getId(),
                logement.getNumero(),
                logement.getImmeuble(),
                logement.getCodeInterne(),
                logement.getTypeLogement(),
                logement.getActive()
        );
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getNumero() {
        return numero;
    }

    public String getImmeuble() {
        return immeuble;
    }

    public String getCodeInterne() {
        return codeInterne;
    }

    public TypeLogement getTypeLogement() {
        return typeLogement;
    }

    public Boolean getActive() {
        return active;
    }
}
