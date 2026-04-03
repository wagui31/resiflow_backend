package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentHistoryItemResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate date;
    private final BigDecimal amount;
    private final String period;

    public PaymentHistoryItemResponse(final LocalDate date, final BigDecimal amount, final String period) {
        this.date = date;
        this.amount = amount;
        this.period = period;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPeriod() {
        return period;
    }
}
