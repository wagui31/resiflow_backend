package com.resiflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private final BigDecimal soldeCagnotte;
    private final Long nombreResidents;
    private final Long nombreEnRetard;
    private final BigDecimal depensesDuMois;
    private final List<VoteResponse> derniersVotes;

    public DashboardResponse(
            final BigDecimal soldeCagnotte,
            final Long nombreResidents,
            final Long nombreEnRetard,
            final BigDecimal depensesDuMois,
            final List<VoteResponse> derniersVotes
    ) {
        this.soldeCagnotte = soldeCagnotte;
        this.nombreResidents = nombreResidents;
        this.nombreEnRetard = nombreEnRetard;
        this.depensesDuMois = depensesDuMois;
        this.derniersVotes = derniersVotes;
    }

    public BigDecimal getSoldeCagnotte() {
        return soldeCagnotte;
    }

    public Long getNombreResidents() {
        return nombreResidents;
    }

    public Long getNombreEnRetard() {
        return nombreEnRetard;
    }

    public BigDecimal getDepensesDuMois() {
        return depensesDuMois;
    }

    public List<VoteResponse> getDerniersVotes() {
        return derniersVotes;
    }
}
