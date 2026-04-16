package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ResidenceViewPaymentStatusResponse {

    private final String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;
    private final boolean nextDueWarning;
    private final List<String> overdueMonths;
    private final PendingPaymentSummary pendingPayment;

    public ResidenceViewPaymentStatusResponse(
            final String status,
            final LocalDate dateFin,
            final boolean nextDueWarning,
            final List<String> overdueMonths,
            final PendingPaymentSummary pendingPayment
    ) {
        this.status = status;
        this.dateFin = dateFin;
        this.nextDueWarning = nextDueWarning;
        this.overdueMonths = overdueMonths;
        this.pendingPayment = pendingPayment;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public boolean isNextDueWarning() {
        return nextDueWarning;
    }

    public List<String> getOverdueMonths() {
        return overdueMonths;
    }

    public PendingPaymentSummary getPendingPayment() {
        return pendingPayment;
    }

    public static class PendingPaymentSummary {

        private final Long id;
        private final BigDecimal montantTotal;
        private final Integer nombreMois;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final LocalDate dateDebut;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final LocalDate dateFin;

        public PendingPaymentSummary(
                final Long id,
                final BigDecimal montantTotal,
                final Integer nombreMois,
                final LocalDate dateDebut,
                final LocalDate dateFin
        ) {
            this.id = id;
            this.montantTotal = montantTotal;
            this.nombreMois = nombreMois;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }

        public Long getId() {
            return id;
        }

        public BigDecimal getMontantTotal() {
            return montantTotal;
        }

        public Integer getNombreMois() {
            return nombreMois;
        }

        public LocalDate getDateDebut() {
            return dateDebut;
        }

        public LocalDate getDateFin() {
            return dateFin;
        }
    }
}
