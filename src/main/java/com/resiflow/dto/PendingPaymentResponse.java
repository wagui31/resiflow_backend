package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PendingPaymentResponse {

    private final Long id;
    private final BigDecimal amount;
    private final Integer months;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;

    public PendingPaymentResponse(
            final Long id,
            final BigDecimal amount,
            final Integer months,
            final LocalDate dateDebut,
            final LocalDate dateFin
    ) {
        this.id = id;
        this.amount = amount;
        this.months = months;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getMonths() {
        return months;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }
}
