package com.resiflow.dto;

public class PaymentStatusMonthResponse {

    private final String month;
    private final boolean paid;

    public PaymentStatusMonthResponse(final String month, final boolean paid) {
        this.month = month;
        this.paid = paid;
    }

    public String getMonth() {
        return month;
    }

    public boolean isPaid() {
        return paid;
    }
}
