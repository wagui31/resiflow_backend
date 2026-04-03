package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.PaiementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UserPaiementHistoryResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;
    private final BigDecimal montantTotal;
    private final PaiementStatus status;

    public UserPaiementHistoryResponse(
            final LocalDate dateDebut,
            final LocalDate dateFin,
            final BigDecimal montantTotal,
            final PaiementStatus status
    ) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.montantTotal = montantTotal;
        this.status = status;
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

    public PaiementStatus getStatus() {
        return status;
    }
}
