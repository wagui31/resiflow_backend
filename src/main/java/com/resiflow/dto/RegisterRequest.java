package com.resiflow.dto;

public class RegisterRequest {

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String residenceCode;
    private Long logementId;
    private String captchaToken;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getResidenceCode() {
        return residenceCode;
    }

    public void setResidenceCode(final String residenceCode) {
        this.residenceCode = residenceCode;
    }

    public Long getLogementId() {
        return logementId;
    }

    public void setLogementId(final Long logementId) {
        this.logementId = logementId;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(final String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
