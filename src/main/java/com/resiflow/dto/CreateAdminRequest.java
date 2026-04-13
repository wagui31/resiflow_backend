package com.resiflow.dto;

public class CreateAdminRequest {

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Long residenceId;
    private Long logementId;

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

    public Long getResidenceId() {
        return residenceId;
    }

    public void setResidenceId(final Long residenceId) {
        this.residenceId = residenceId;
    }

    public Long getLogementId() {
        return logementId;
    }

    public void setLogementId(final Long logementId) {
        this.logementId = logementId;
    }
}
