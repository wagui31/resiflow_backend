package com.resiflow.dto;

import com.resiflow.entity.TypeLogement;

public class CreateLogementsBulkRequest {

    private Long residenceId;
    private TypeLogement typeLogement;
    private String numeroDebut;
    private String numeroFin;
    private String immeuble;
    private String etage;
    private String codePostal;
    private String adresse;

    public Long getResidenceId() {
        return residenceId;
    }

    public void setResidenceId(final Long residenceId) {
        this.residenceId = residenceId;
    }

    public TypeLogement getTypeLogement() {
        return typeLogement;
    }

    public void setTypeLogement(final TypeLogement typeLogement) {
        this.typeLogement = typeLogement;
    }

    public String getNumeroDebut() {
        return numeroDebut;
    }

    public void setNumeroDebut(final String numeroDebut) {
        this.numeroDebut = numeroDebut;
    }

    public String getNumeroFin() {
        return numeroFin;
    }

    public void setNumeroFin(final String numeroFin) {
        this.numeroFin = numeroFin;
    }

    public String getImmeuble() {
        return immeuble;
    }

    public void setImmeuble(final String immeuble) {
        this.immeuble = immeuble;
    }

    public String getEtage() {
        return etage;
    }

    public void setEtage(final String etage) {
        this.etage = etage;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(final String codePostal) {
        this.codePostal = codePostal;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(final String adresse) {
        this.adresse = adresse;
    }
}
