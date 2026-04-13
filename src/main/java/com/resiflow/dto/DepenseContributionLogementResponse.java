package com.resiflow.dto;

import java.math.BigDecimal;

public class DepenseContributionLogementResponse {

    private final Long logementId;
    private final String logementLabel;
    private final String codeInterne;
    private final BigDecimal montantDu;
    private final BigDecimal montantPaye;
    private final String statut;

    public DepenseContributionLogementResponse(
            final Long logementId,
            final String logementLabel,
            final String codeInterne,
            final BigDecimal montantDu,
            final BigDecimal montantPaye,
            final String statut
    ) {
        this.logementId = logementId;
        this.logementLabel = logementLabel;
        this.codeInterne = codeInterne;
        this.montantDu = montantDu;
        this.montantPaye = montantPaye;
        this.statut = statut;
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getLogementLabel() {
        return logementLabel;
    }

    public String getCodeInterne() {
        return codeInterne;
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
