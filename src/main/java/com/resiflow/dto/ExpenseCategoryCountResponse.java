package com.resiflow.dto;

public class ExpenseCategoryCountResponse {

    private final Long categorieId;
    private final String categorieNom;
    private final Long nombreDepenses;

    public ExpenseCategoryCountResponse(
            final Long categorieId,
            final String categorieNom,
            final Long nombreDepenses
    ) {
        this.categorieId = categorieId;
        this.categorieNom = categorieNom;
        this.nombreDepenses = nombreDepenses;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public Long getNombreDepenses() {
        return nombreDepenses;
    }
}
