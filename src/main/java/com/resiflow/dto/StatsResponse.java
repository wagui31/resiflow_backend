package com.resiflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class StatsResponse {

    private final BigDecimal totalContributions;
    private final BigDecimal totalDepenses;
    private final BigDecimal soldeActuel;
    private final List<TopPayeurResponse> topPayeurs;
    private final List<EvolutionCagnotteResponse> evolutionCagnotte;

    public StatsResponse(
            final BigDecimal totalContributions,
            final BigDecimal totalDepenses,
            final BigDecimal soldeActuel,
            final List<TopPayeurResponse> topPayeurs,
            final List<EvolutionCagnotteResponse> evolutionCagnotte
    ) {
        this.totalContributions = totalContributions;
        this.totalDepenses = totalDepenses;
        this.soldeActuel = soldeActuel;
        this.topPayeurs = topPayeurs;
        this.evolutionCagnotte = evolutionCagnotte;
    }

    public BigDecimal getTotalContributions() {
        return totalContributions;
    }

    public BigDecimal getTotalDepenses() {
        return totalDepenses;
    }

    public BigDecimal getSoldeActuel() {
        return soldeActuel;
    }

    public List<TopPayeurResponse> getTopPayeurs() {
        return topPayeurs;
    }

    public List<EvolutionCagnotteResponse> getEvolutionCagnotte() {
        return evolutionCagnotte;
    }
}
