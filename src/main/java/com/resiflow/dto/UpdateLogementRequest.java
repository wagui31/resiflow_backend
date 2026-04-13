package com.resiflow.dto;

import com.resiflow.entity.TypeLogement;

public class UpdateLogementRequest {

    private TypeLogement typeLogement;
    private String numero;
    private String immeuble;
    private String etage;
    private String codePostal;
    private String adresse;

    public TypeLogement getTypeLogement() {
        return typeLogement;
    }

    public void setTypeLogement(final TypeLogement typeLogement) {
        this.typeLogement = typeLogement;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(final String numero) {
        this.numero = numero;
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
