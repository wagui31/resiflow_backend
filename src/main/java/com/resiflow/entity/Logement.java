package com.resiflow.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "logements")
public class Logement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residence_id", nullable = false)
    private Residence residence;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_logement", nullable = false)
    private TypeLogement typeLogement;

    @Column(nullable = false)
    private String numero;

    @Column
    private String immeuble;

    @Column
    private String etage;

    @Column(name = "code_postal")
    private String codePostal;

    @Column
    private String adresse;

    @Column(name = "code_interne", nullable = false, unique = true)
    private String codeInterne;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "date_activation")
    private LocalDateTime dateActivation;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Residence getResidence() {
        return residence;
    }

    public void setResidence(final Residence residence) {
        this.residence = residence;
    }

    public Long getResidenceId() {
        return residence == null ? null : residence.getId();
    }

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

    public String getCodeInterne() {
        return codeInterne;
    }

    public void setCodeInterne(final String codeInterne) {
        this.codeInterne = codeInterne;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public LocalDateTime getDateActivation() {
        return dateActivation;
    }

    public void setDateActivation(final LocalDateTime dateActivation) {
        this.dateActivation = dateActivation;
    }
}
