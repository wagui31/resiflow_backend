package com.resiflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_months")
public class PaymentMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "logement_id", nullable = false)
    private Logement logement;

    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentMonthStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Paiement payment;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Logement getLogement() {
        return logement;
    }

    public void setLogement(final Logement logement) {
        this.logement = logement;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(final String month) {
        this.month = month;
    }

    public PaymentMonthStatus getStatus() {
        return status;
    }

    public void setStatus(final PaymentMonthStatus status) {
        this.status = status;
    }

    public Paiement getPayment() {
        return payment;
    }

    public void setPayment(final Paiement payment) {
        this.payment = payment;
    }
}
