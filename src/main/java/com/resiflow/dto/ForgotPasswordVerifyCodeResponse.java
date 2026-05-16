package com.resiflow.dto;

import java.time.LocalDateTime;

public class ForgotPasswordVerifyCodeResponse {

    private final String resetSessionToken;
    private final LocalDateTime resetSessionExpiresAt;

    public ForgotPasswordVerifyCodeResponse(
            final String resetSessionToken,
            final LocalDateTime resetSessionExpiresAt
    ) {
        this.resetSessionToken = resetSessionToken;
        this.resetSessionExpiresAt = resetSessionExpiresAt;
    }

    public String getResetSessionToken() {
        return resetSessionToken;
    }

    public LocalDateTime getResetSessionExpiresAt() {
        return resetSessionExpiresAt;
    }
}
