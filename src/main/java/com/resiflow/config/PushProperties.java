package com.resiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.push")
public class PushProperties {

    private boolean enabled;
    private Firebase firebase = new Firebase();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public void setFirebase(final Firebase firebase) {
        this.firebase = firebase == null ? new Firebase() : firebase;
    }

    public static class Firebase {

        private String projectId;
        private String credentialsFile;
        private String credentialsBase64;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(final String projectId) {
            this.projectId = projectId;
        }

        public String getCredentialsFile() {
            return credentialsFile;
        }

        public void setCredentialsFile(final String credentialsFile) {
            this.credentialsFile = credentialsFile;
        }

        public String getCredentialsBase64() {
            return credentialsBase64;
        }

        public void setCredentialsBase64(final String credentialsBase64) {
            this.credentialsBase64 = credentialsBase64;
        }
    }
}
