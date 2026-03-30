package com.resiflow.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residence_id", nullable = false)
    private Residence residence;

    @Column(name = "nombre_mois", nullable = false)
    private Integer nombreMois;

    @Column(name = "montant_mensuel", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantMensuel;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par", nullable = false)
    private User creePar;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(final User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Residence getResidence() {
        return residence;
    }

    public void setResidence(final Residence residence) {
        this.residence = residence;
    }

    public Integer getNombreMois() {
        return nombreMois;
    }

    public void setNombreMois(final Integer nombreMois) {
        this.nombreMois = nombreMois;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public void setMontantMensuel(final BigDecimal montantMensuel) {
        this.montantMensuel = montantMensuel;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(final BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(final LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(final LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(final LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public User getCreePar() {
        return creePar;
    }

    public void setCreePar(final User creePar) {
        this.creePar = creePar;
    }

    @PrePersist
    public void prePersist() {
        if (datePaiement == null) {
            datePaiement = LocalDateTime.now();
        }
    }
}
