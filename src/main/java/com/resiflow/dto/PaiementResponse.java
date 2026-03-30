package com.resiflow.dto;

import com.resiflow.entity.Paiement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaiementResponse {

    private final Long id;
    private final Long utilisateurId;
    private final Long residenceId;
    private final Integer nombreMois;
    private final BigDecimal montantMensuel;
    private final BigDecimal montantTotal;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final LocalDateTime datePaiement;
    private final Long creeParId;

    public PaiementResponse(
            final Long id,
            final Long utilisateurId,
            final Long residenceId,
            final Integer nombreMois,
            final BigDecimal montantMensuel,
            final BigDecimal montantTotal,
            final LocalDate dateDebut,
            final LocalDate dateFin,
            final LocalDateTime datePaiement,
            final Long creeParId
    ) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.residenceId = residenceId;
        this.nombreMois = nombreMois;
        this.montantMensuel = montantMensuel;
        this.montantTotal = montantTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.datePaiement = datePaiement;
        this.creeParId = creeParId;
    }

    public static PaiementResponse fromEntity(final Paiement paiement) {
        return new PaiementResponse(
                paiement.getId(),
                paiement.getUtilisateur().getId(),
                paiement.getResidence().getId(),
                paiement.getNombreMois(),
                paiement.getMontantMensuel(),
                paiement.getMontantTotal(),
                paiement.getDateDebut(),
                paiement.getDateFin(),
                paiement.getDatePaiement(),
                paiement.getCreePar().getId()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Integer getNombreMois() {
        return nombreMois;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public Long getCreeParId() {
        return creeParId;
    }
}
