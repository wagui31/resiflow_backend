package com.resiflow.dto;

public class RegisterRequest {

    private String email;
    private String password;
    private String residenceCode;
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

    public String getResidenceCode() {
        return residenceCode;
    }

    public void setResidenceCode(final String residenceCode) {
        this.residenceCode = residenceCode;
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
