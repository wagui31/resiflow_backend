package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.TypePaiement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaiementResponse {

    private final Long id;
    private final Long logementId;
    private final Long residenceId;
    private final LogementSummaryResponse logement;
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
    private final TypePaiement typePaiement;
    private final Long depenseId;

    public PaiementResponse(
            final Long id,
            final Long logementId,
            final Long residenceId,
            final LogementSummaryResponse logement,
            final Integer nombreMois,
            final BigDecimal montantMensuel,
            final BigDecimal montantTotal,
            final LocalDate dateDebut,
            final LocalDate dateFin,
            final LocalDateTime datePaiement,
            final Long creeParId,
            final PaiementStatus status,
            final TypePaiement typePaiement,
            final Long depenseId
    ) {
        this.id = id;
        this.logementId = logementId;
        this.residenceId = residenceId;
        this.logement = logement;
        this.nombreMois = nombreMois;
        this.montantMensuel = montantMensuel;
        this.montantTotal = montantTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.datePaiement = datePaiement;
        this.creeParId = creeParId;
        this.status = status;
        this.typePaiement = typePaiement;
        this.depenseId = depenseId;
    }

    public static PaiementResponse fromEntity(final Paiement paiement) {
        return new PaiementResponse(
                paiement.getId(),
                paiement.getLogementId(),
                paiement.getResidence().getId(),
                LogementSummaryResponse.fromEntity(paiement.getLogement()),
                paiement.getNombreMois(),
                paiement.getMontantMensuel(),
                paiement.getMontantTotal(),
                paiement.getDateDebut(),
                paiement.getDateFin(),
                paiement.getDatePaiement(),
                paiement.getCreePar().getId(),
                paiement.getStatus(),
                paiement.getTypePaiement(),
                paiement.getDepense() == null ? null : paiement.getDepense().getId()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getLogementId() {
        return logementId;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public LogementSummaryResponse getLogement() {
        return logement;
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

    public TypePaiement getTypePaiement() {
        return typePaiement;
    }

    public Long getDepenseId() {
        return depenseId;
    }
}
