package com.resiflow.dto;

import com.resiflow.entity.TypeLogement;
import java.util.List;

public class ResidenceAlertLogementResponse {

    private final Long logementId;
    private final String alertType;
    private final String label;
    private final String codeInterne;
    private final TypeLogement typeLogement;
    private final String numero;
    private final String immeuble;
    private final String etage;
    private final Boolean active;
    private final Integer overdueMonthsCount;
    private final List<String> overdueMonths;

    public ResidenceAlertLogementResponse(
            final Long logementId,
            final String alertType,
            final String label,
            final String codeInterne,
            final TypeLogement typeLogement,
            final String numero,
            final String immeuble,
            final String etage,
            final Boolean active,
            final Integer overdueMonthsCount,
            final List<String> overdueMonths
    ) {
        this.logementId = logementId;
        this.alertType = alertType;
        this.label = label;
        this.codeInterne = codeInterne;
        this.typeLogement = typeLogement;
        this.numero = numero;
        this.immeuble = immeuble;
        this.etage = etage;
        this.active = active;
        this.overdueMonthsCount = overdueMonthsCount;
        this.overdueMonths = overdueMonths;
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getLabel() {
        return label;
    }

    public String getCodeInterne() {
        return codeInterne;
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

    public Boolean getActive() {
        return active;
    }

    public Integer getOverdueMonthsCount() {
        return overdueMonthsCount;
    }

    public List<String> getOverdueMonths() {
        return overdueMonths;
    }
}
