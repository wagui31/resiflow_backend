package com.resiflow.dto;

public class UpdateCurrentUserRequest {

    private String firstName;
    private String lastName;
    private String numeroImmeuble;
    private String codeLogement;

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
