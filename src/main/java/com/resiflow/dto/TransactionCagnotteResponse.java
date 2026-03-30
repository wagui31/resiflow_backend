package com.resiflow.dto;

import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionCagnotteResponse {

    private final Long id;
    private final Long residenceId;
    private final Long userId;
    private final TypeTransactionCagnotte type;
    private final BigDecimal montant;
    private final Long referenceId;
    private final LocalDateTime dateCreation;

    public TransactionCagnotteResponse(
            final Long id,
            final Long residenceId,
            final Long userId,
            final TypeTransactionCagnotte type,
            final BigDecimal montant,
            final Long referenceId,
            final LocalDateTime dateCreation
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.userId = userId;
        this.type = type;
        this.montant = montant;
        this.referenceId = referenceId;
        this.dateCreation = dateCreation;
    }

    public static TransactionCagnotteResponse fromEntity(final TransactionCagnotte transaction) {
        return new TransactionCagnotteResponse(
                transaction.getId(),
                transaction.getResidence().getId(),
                transaction.getUser() == null ? null : transaction.getUser().getId(),
                transaction.getType(),
                transaction.getMontant(),
                transaction.getReferenceId(),
                transaction.getDateCreation()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getUserId() {
        return userId;
    }

    public TypeTransactionCagnotte getType() {
        return type;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}
