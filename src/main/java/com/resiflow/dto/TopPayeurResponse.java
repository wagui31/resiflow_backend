package com.resiflow.dto;

import java.math.BigDecimal;

public class TopPayeurResponse {

    private final Long userId;
    private final String email;
    private final BigDecimal totalPaye;

    public TopPayeurResponse(final Long userId, final String email, final BigDecimal totalPaye) {
        this.userId = userId;
        this.email = email;
        this.totalPaye = totalPaye;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getTotalPaye() {
        return totalPaye;
    }
}
