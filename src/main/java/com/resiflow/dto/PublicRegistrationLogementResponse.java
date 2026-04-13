package com.resiflow.dto;

import com.resiflow.entity.Logement;
import com.resiflow.entity.TypeLogement;

public class PublicRegistrationLogementResponse {

    private final Long logementId;
    private final TypeLogement typeLogement;
    private final String numero;
    private final String immeuble;
    private final String etage;
    private final String codeInterne;
    private final Boolean active;
    private final Long occupiedCount;
    private final Integer maxOccupants;
    private final boolean full;

    public PublicRegistrationLogementResponse(
            final Long logementId,
            final TypeLogement typeLogement,
            final String numero,
            final String immeuble,
            final String etage,
            final String codeInterne,
            final Boolean active,
            final Long occupiedCount,
            final Integer maxOccupants,
            final boolean full
    ) {
        this.logementId = logementId;
        this.typeLogement = typeLogement;
        this.numero = numero;
        this.immeuble = immeuble;
        this.etage = etage;
        this.codeInterne = codeInterne;
        this.active = active;
        this.occupiedCount = occupiedCount;
        this.maxOccupants = maxOccupants;
        this.full = full;
    }

    public static PublicRegistrationLogementResponse fromEntity(
            final Logement logement,
            final long occupiedCount,
            final int maxOccupants
    ) {
        return new PublicRegistrationLogementResponse(
                logement.getId(),
                logement.getTypeLogement(),
                logement.getNumero(),
                logement.getImmeuble(),
                logement.getEtage(),
                logement.getCodeInterne(),
                logement.getActive(),
                occupiedCount,
                maxOccupants,
                occupiedCount >= maxOccupants
        );
    }

    public Long getLogementId() {
        return logementId;
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

    public String getCodeInterne() {
        return codeInterne;
    }

    public Boolean getActive() {
        return active;
    }

    public Long getOccupiedCount() {
        return occupiedCount;
    }

    public Integer getMaxOccupants() {
        return maxOccupants;
    }

    public boolean isFull() {
        return full;
    }
}
