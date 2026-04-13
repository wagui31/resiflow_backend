package com.resiflow.dto;

import java.util.List;

public class ResidenceViewLogementCardResponse {

    private final LogementResponse logement;
    private final LogementOccupancyResponse occupancy;
    private final ResidenceViewPaymentStatusResponse payment;
    private final List<ResidenceViewResidentResponse> residents;

    public ResidenceViewLogementCardResponse(
            final LogementResponse logement,
            final LogementOccupancyResponse occupancy,
            final ResidenceViewPaymentStatusResponse payment,
            final List<ResidenceViewResidentResponse> residents
    ) {
        this.logement = logement;
        this.occupancy = occupancy;
        this.payment = payment;
        this.residents = residents;
    }

    public LogementResponse getLogement() {
        return logement;
    }

    public LogementOccupancyResponse getOccupancy() {
        return occupancy;
    }

    public ResidenceViewPaymentStatusResponse getPayment() {
        return payment;
    }

    public List<ResidenceViewResidentResponse> getResidents() {
        return residents;
    }
}
