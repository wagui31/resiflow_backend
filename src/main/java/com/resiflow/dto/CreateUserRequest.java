package com.resiflow.dto;

public class CreateUserRequest {

    private String email;
    private String password;
    private Long residenceId;
    private String numeroImmeuble;
    private String codeLogement;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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

    public String getNumeroImmeuble() {
        return numeroImmeuble;
    }

    public void setNumeroImmeuble(final String numeroImmeuble) {
        this.numeroImmeuble = numeroImmeuble;
    }

    public String getCodeLogement() {
        return codeLogement;
    }

    public void setCodeLogement(final String codeLogement) {
        this.codeLogement = codeLogement;
    }
}
