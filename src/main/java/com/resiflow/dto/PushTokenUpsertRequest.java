package com.resiflow.dto;

import com.resiflow.entity.PushTokenPlatform;

public class PushTokenUpsertRequest {

    private String token;
    private PushTokenPlatform platform;
    private String installationId;
    private String deviceName;
    private String appVersion;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public PushTokenPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(final PushTokenPlatform platform) {
        this.platform = platform;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(final String installationId) {
        this.installationId = installationId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(final String appVersion) {
        this.appVersion = appVersion;
    }
}
