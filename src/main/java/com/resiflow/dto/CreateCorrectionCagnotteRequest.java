package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateCorrectionCagnotteRequest {

    private BigDecimal nouveauSolde;
    private String motif;

    public BigDecimal getNouveauSolde() {
        return nouveauSolde;
    }

    public void setNouveauSolde(final BigDecimal nouveauSolde) {
        this.nouveauSolde = nouveauSolde;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(final String motif) {
        this.motif = motif;
    }
}
