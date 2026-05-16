package com.resiflow.dto;

public class PushTokenDeactivateRequest {

    private String token;
    private String installationId;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(final String installationId) {
        this.installationId = installationId;
    }
}
