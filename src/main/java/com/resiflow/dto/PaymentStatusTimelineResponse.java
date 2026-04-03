package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class PaymentStatusTimelineResponse {

    private final String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateFin;
    private final boolean nextDueWarning;
    private final PendingPaymentResponse pendingPayment;
    private final List<PaymentStatusMonthResponse> months;
    private final List<PaymentHistoryItemResponse> history;

    public PaymentStatusTimelineResponse(
            final String status,
            final LocalDate dateFin,
            final boolean nextDueWarning,
            final PendingPaymentResponse pendingPayment,
            final List<PaymentStatusMonthResponse> months,
            final List<PaymentHistoryItemResponse> history
    ) {
        this.status = status;
        this.dateFin = dateFin;
        this.nextDueWarning = nextDueWarning;
        this.pendingPayment = pendingPayment;
        this.months = months;
        this.history = history;
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

    public PendingPaymentResponse getPendingPayment() {
        return pendingPayment;
    }

    public List<PaymentStatusMonthResponse> getMonths() {
        return months;
    }

    public List<PaymentHistoryItemResponse> getHistory() {
        return history;
    }
}
