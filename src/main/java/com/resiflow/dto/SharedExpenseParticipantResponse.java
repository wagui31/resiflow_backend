package com.resiflow.dto;

import java.math.BigDecimal;

public class SharedExpenseParticipantResponse {

    private final Long utilisateurId;
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final BigDecimal montantDu;
    private final BigDecimal montantPaye;
    private final String statut;

    public SharedExpenseParticipantResponse(
            final Long utilisateurId,
            final String firstName,
            final String lastName,
            final String fullName,
            final BigDecimal montantDu,
            final BigDecimal montantPaye,
            final String statut
    ) {
        this.utilisateurId = utilisateurId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.montantDu = montantDu;
        this.montantPaye = montantPaye;
        this.statut = statut;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public BigDecimal getMontantDu() {
        return montantDu;
    }

    public BigDecimal getMontantPaye() {
        return montantPaye;
    }

    public String getStatut() {
        return statut;
    }
}
