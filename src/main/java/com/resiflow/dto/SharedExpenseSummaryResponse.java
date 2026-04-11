package com.resiflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SharedExpenseSummaryResponse {

    private final Long id;
    private final Long residenceId;
    private final Long categorieId;
    private final String categorieNom;
    private final String description;
    private final BigDecimal montantTotal;
    private final BigDecimal montantPayeTotal;
    private final BigDecimal montantParPersonne;
    private final Integer nombreParticipantsRestants;
    private final LocalDateTime dateCreation;
    private final LocalDateTime dateValidation;
    private final ExpenseUserSummaryResponse creePar;
    private final List<SharedExpenseParticipantResponse> participants;

    public SharedExpenseSummaryResponse(
            final Long id,
            final Long residenceId,
            final Long categorieId,
            final String categorieNom,
            final String description,
            final BigDecimal montantTotal,
            final BigDecimal montantPayeTotal,
            final BigDecimal montantParPersonne,
            final Integer nombreParticipantsRestants,
            final LocalDateTime dateCreation,
            final LocalDateTime dateValidation,
            final ExpenseUserSummaryResponse creePar,
            final List<SharedExpenseParticipantResponse> participants
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.categorieId = categorieId;
        this.categorieNom = categorieNom;
        this.description = description;
        this.montantTotal = montantTotal;
        this.montantPayeTotal = montantPayeTotal;
        this.montantParPersonne = montantParPersonne;
        this.nombreParticipantsRestants = nombreParticipantsRestants;
        this.dateCreation = dateCreation;
        this.dateValidation = dateValidation;
        this.creePar = creePar;
        this.participants = participants;
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public BigDecimal getMontantPayeTotal() {
        return montantPayeTotal;
    }

    public BigDecimal getMontantParPersonne() {
        return montantParPersonne;
    }

    public Integer getNombreParticipantsRestants() {
        return nombreParticipantsRestants;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public ExpenseUserSummaryResponse getCreePar() {
        return creePar;
    }

    public List<SharedExpenseParticipantResponse> getParticipants() {
        return participants;
    }
}
