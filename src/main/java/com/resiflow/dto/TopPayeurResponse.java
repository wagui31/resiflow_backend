package com.resiflow.dto;

import java.math.BigDecimal;

public class TopPayeurResponse {

    private final Long logementId;
    private final String label;
    private final BigDecimal totalPaye;

    public TopPayeurResponse(final Long logementId, final String label, final BigDecimal totalPaye) {
        this.logementId = logementId;
        this.label = label;
        this.totalPaye = totalPaye;
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getTotalPaye() {
        return totalPaye;
    }
}
