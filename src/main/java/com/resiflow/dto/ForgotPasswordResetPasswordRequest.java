package com.resiflow.dto;

public class ForgotPasswordResetPasswordRequest {

    private String resetSessionToken;
    private String newPassword;
    private String confirmPassword;

    public String getResetSessionToken() {
        return resetSessionToken;
    }

    public void setResetSessionToken(final String resetSessionToken) {
        this.resetSessionToken = resetSessionToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
