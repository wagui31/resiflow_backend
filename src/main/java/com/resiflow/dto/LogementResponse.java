package com.resiflow.dto;

import com.resiflow.entity.Logement;
import com.resiflow.entity.TypeLogement;
import java.time.LocalDateTime;

public class LogementResponse {

    private final Long id;
    private final Long residenceId;
    private final TypeLogement typeLogement;
    private final String numero;
    private final String immeuble;
    private final String etage;
    private final String codePostal;
    private final String adresse;
    private final String codeInterne;
    private final Boolean active;
    private final LocalDateTime dateActivation;

    public LogementResponse(
            final Long id,
            final Long residenceId,
            final TypeLogement typeLogement,
            final String numero,
            final String immeuble,
            final String etage,
            final String codePostal,
            final String adresse,
            final String codeInterne,
            final Boolean active,
            final LocalDateTime dateActivation
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.typeLogement = typeLogement;
        this.numero = numero;
        this.immeuble = immeuble;
        this.etage = etage;
        this.codePostal = codePostal;
        this.adresse = adresse;
        this.codeInterne = codeInterne;
        this.active = active;
        this.dateActivation = dateActivation;
    }

    public static LogementResponse fromEntity(final Logement logement) {
        return new LogementResponse(
                logement.getId(),
                logement.getResidenceId(),
                logement.getTypeLogement(),
                logement.getNumero(),
                logement.getImmeuble(),
                logement.getEtage(),
                logement.getCodePostal(),
                logement.getAdresse(),
                logement.getCodeInterne(),
                logement.getActive(),
                logement.getDateActivation()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public TypeLogement getTypeLogement() {
        return typeLogement;
    }

    public String getNumero() {
        return numero;
    }

    public String getImmeuble() {
        return immeuble;
    }

    public String getEtage() {
        return etage;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getCodeInterne() {
        return codeInterne;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getDateActivation() {
        return dateActivation;
    }
}
