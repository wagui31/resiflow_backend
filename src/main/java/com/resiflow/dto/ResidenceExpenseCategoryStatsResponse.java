package com.resiflow.dto;

import java.util.List;

public class ResidenceExpenseCategoryStatsResponse {

    private final Long residenceId;
    private final List<ExpenseCategoryCountResponse> categories;

    public ResidenceExpenseCategoryStatsResponse(
            final Long residenceId,
            final List<ExpenseCategoryCountResponse> categories
    ) {
        this.residenceId = residenceId;
        this.categories = categories;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public List<ExpenseCategoryCountResponse> getCategories() {
        return categories;
    }
}
