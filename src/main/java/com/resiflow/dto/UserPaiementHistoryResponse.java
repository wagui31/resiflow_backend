package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.StatutPaiement;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UserPaiementHistoryResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;
    private final BigDecimal montantTotal;
    private final StatutPaiement statut;

    public UserPaiementHistoryResponse(
            final LocalDate dateDebut,
            final LocalDate dateFin,
            final BigDecimal montantTotal,
            final StatutPaiement statut
    ) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.montantTotal = montantTotal;
        this.statut = statut;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public StatutPaiement getStatut() {
        return statut;
    }
}
