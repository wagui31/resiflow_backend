package com.resiflow.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "depenses")
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residence_id", nullable = false)
    private Residence residence;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private CategorieDepense categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDepense statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par", nullable = false)
    private User creePar;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par")
    private User validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

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

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public CategorieDepense getCategorie() {
        return categorie;
    }

    public void setCategorie(final CategorieDepense categorie) {
        this.categorie = categorie;
    }

    public StatutDepense getStatut() {
        return statut;
    }

    public void setStatut(final StatutDepense statut) {
        this.statut = statut;
    }

    public User getCreePar() {
        return creePar;
    }

    public void setCreePar(final User creePar) {
        this.creePar = creePar;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(final LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public User getValidePar() {
        return validePar;
    }

    public void setValidePar(final User validePar) {
        this.validePar = validePar;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(final LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
