package com.resiflow.dto;

import com.resiflow.entity.CorrectionCagnotte;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CorrectionCagnotteResponse {

    private final Long id;
    private final Long residenceId;
    private final BigDecimal ancienSolde;
    private final BigDecimal nouveauSolde;
    private final BigDecimal delta;
    private final String motif;
    private final Long creeParUserId;
    private final LocalDateTime dateCreation;

    public CorrectionCagnotteResponse(
            final Long id,
            final Long residenceId,
            final BigDecimal ancienSolde,
            final BigDecimal nouveauSolde,
            final BigDecimal delta,
            final String motif,
            final Long creeParUserId,
            final LocalDateTime dateCreation
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.ancienSolde = ancienSolde;
        this.nouveauSolde = nouveauSolde;
        this.delta = delta;
        this.motif = motif;
        this.creeParUserId = creeParUserId;
        this.dateCreation = dateCreation;
    }

    public static CorrectionCagnotteResponse fromEntity(final CorrectionCagnotte correction) {
        return new CorrectionCagnotteResponse(
                correction.getId(),
                correction.getResidence().getId(),
                correction.getAncienSolde(),
                correction.getNouveauSolde(),
                correction.getDelta(),
                correction.getMotif(),
                correction.getCreePar().getId(),
                correction.getDateCreation()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public BigDecimal getAncienSolde() {
        return ancienSolde;
    }

    public BigDecimal getNouveauSolde() {
        return nouveauSolde;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public String getMotif() {
        return motif;
    }

    public Long getCreeParUserId() {
        return creeParUserId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}
