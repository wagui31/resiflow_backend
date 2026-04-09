package com.resiflow.dto;

import java.math.BigDecimal;

public class DepenseContributionUserResponse {

    private final Long utilisateurId;
    private final String utilisateurEmail;
    private final BigDecimal montantDu;
    private final BigDecimal montantPaye;
    private final String statut;

    public DepenseContributionUserResponse(
            final Long utilisateurId,
            final String utilisateurEmail,
            final BigDecimal montantDu,
            final BigDecimal montantPaye,
            final String statut
    ) {
        this.utilisateurId = utilisateurId;
        this.utilisateurEmail = utilisateurEmail;
        this.montantDu = montantDu;
        this.montantPaye = montantPaye;
        this.statut = statut;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public String getUtilisateurEmail() {
        return utilisateurEmail;
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
