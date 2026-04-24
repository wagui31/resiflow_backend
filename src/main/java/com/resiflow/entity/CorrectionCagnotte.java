package com.resiflow.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "corrections_cagnotte")
public class CorrectionCagnotte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residence_id", nullable = false)
    private Residence residence;

    @Column(name = "ancien_solde", nullable = false, precision = 12, scale = 2)
    private BigDecimal ancienSolde;

    @Column(name = "nouveau_solde", nullable = false, precision = 12, scale = 2)
    private BigDecimal nouveauSolde;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal delta;

    @Column(nullable = false, length = 500)
    private String motif;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par", nullable = false)
    private User creePar;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

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

    public BigDecimal getAncienSolde() {
        return ancienSolde;
    }

    public void setAncienSolde(final BigDecimal ancienSolde) {
        this.ancienSolde = ancienSolde;
    }

    public BigDecimal getNouveauSolde() {
        return nouveauSolde;
    }

    public void setNouveauSolde(final BigDecimal nouveauSolde) {
        this.nouveauSolde = nouveauSolde;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(final BigDecimal delta) {
        this.delta = delta;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(final String motif) {
        this.motif = motif;
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

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
