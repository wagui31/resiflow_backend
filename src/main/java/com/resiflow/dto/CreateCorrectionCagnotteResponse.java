package com.resiflow.dto;

import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateCorrectionCagnotteResponse {

    private final Long residenceId;
    private final BigDecimal ancienSolde;
    private final BigDecimal nouveauSolde;
    private final BigDecimal delta;
    private final Long correctionId;
    private final Long transactionId;
    private final TypeTransactionCagnotte typeTransaction;
    private final LocalDateTime dateCreation;

    public CreateCorrectionCagnotteResponse(
            final Long residenceId,
            final BigDecimal ancienSolde,
            final BigDecimal nouveauSolde,
            final BigDecimal delta,
            final Long correctionId,
            final Long transactionId,
            final TypeTransactionCagnotte typeTransaction,
            final LocalDateTime dateCreation
    ) {
        this.residenceId = residenceId;
        this.ancienSolde = ancienSolde;
        this.nouveauSolde = nouveauSolde;
        this.delta = delta;
        this.correctionId = correctionId;
        this.transactionId = transactionId;
        this.typeTransaction = typeTransaction;
        this.dateCreation = dateCreation;
    }

    public static CreateCorrectionCagnotteResponse of(
            final CorrectionCagnotteResponse correction,
            final TransactionCagnotte transaction
    ) {
        return new CreateCorrectionCagnotteResponse(
                correction.getResidenceId(),
                correction.getAncienSolde(),
                correction.getNouveauSolde(),
                correction.getDelta(),
                correction.getId(),
                transaction.getId(),
                transaction.getType(),
                transaction.getDateCreation()
        );
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

    public Long getCorrectionId() {
        return correctionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public TypeTransactionCagnotte getTypeTransaction() {
        return typeTransaction;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}
