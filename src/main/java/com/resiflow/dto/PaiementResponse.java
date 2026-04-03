package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaiementResponse {

    private final Long id;
    private final Long utilisateurId;
    private final String utilisateurEmail;
    private final Long residenceId;
    private final Integer nombreMois;
    private final BigDecimal montantMensuel;
    private final BigDecimal montantTotal;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;
    private final LocalDateTime datePaiement;
    private final Long creeParId;
    private final PaiementStatus status;

    public PaiementResponse(
            final Long id,
            final Long utilisateurId,
            final String utilisateurEmail,
            final Long residenceId,
            final Integer nombreMois,
            final BigDecimal montantMensuel,
            final BigDecimal montantTotal,
            final LocalDate dateDebut,
            final LocalDate dateFin,
            final LocalDateTime datePaiement,
            final Long creeParId,
            final PaiementStatus status
    ) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.utilisateurEmail = utilisateurEmail;
        this.residenceId = residenceId;
        this.nombreMois = nombreMois;
        this.montantMensuel = montantMensuel;
        this.montantTotal = montantTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.datePaiement = datePaiement;
        this.creeParId = creeParId;
        this.status = status;
    }

    public static PaiementResponse fromEntity(final Paiement paiement) {
        return new PaiementResponse(
                paiement.getId(),
                paiement.getUtilisateur().getId(),
                paiement.getUtilisateur().getEmail(),
                paiement.getResidence().getId(),
                paiement.getNombreMois(),
                paiement.getMontantMensuel(),
                paiement.getMontantTotal(),
                paiement.getDateDebut(),
                paiement.getDateFin(),
                paiement.getDatePaiement(),
                paiement.getCreePar().getId(),
                paiement.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public String getUtilisateurEmail() {
        return utilisateurEmail;
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

    public PaiementStatus getStatus() {
        return status;
    }
}
