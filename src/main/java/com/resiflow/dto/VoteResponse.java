package com.resiflow.dto;

import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteStatut;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoteResponse {

    private final Long id;
    private final Long residenceId;
    private final String titre;
    private final String description;
    private final BigDecimal montantEstime;
    private final VoteStatut statut;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFin;
    private final Long creeParId;
    private final Long depenseId;

    public VoteResponse(
            final Long id,
            final Long residenceId,
            final String titre,
            final String description,
            final BigDecimal montantEstime,
            final VoteStatut statut,
            final LocalDateTime dateDebut,
            final LocalDateTime dateFin,
            final Long creeParId,
            final Long depenseId
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.titre = titre;
        this.description = description;
        this.montantEstime = montantEstime;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.creeParId = creeParId;
        this.depenseId = depenseId;
    }

    public static VoteResponse fromEntity(final Vote vote) {
        return new VoteResponse(
                vote.getId(),
                vote.getResidence().getId(),
                vote.getTitre(),
                vote.getDescription(),
                vote.getMontantEstime(),
                vote.getStatut(),
                vote.getDateDebut(),
                vote.getDateFin(),
                vote.getCreePar().getId(),
                vote.getDepense() == null ? null : vote.getDepense().getId()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getMontantEstime() {
        return montantEstime;
    }

    public VoteStatut getStatut() {
        return statut;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public Long getCreeParId() {
        return creeParId;
    }

    public Long getDepenseId() {
        return depenseId;
    }
}
