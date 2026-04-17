package com.resiflow.dto;

import com.resiflow.entity.VoteStatut;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VoteOverviewResponse {

    private final Long id;
    private final Long residenceId;
    private final String titre;
    private final String description;
    private final BigDecimal montantEstime;
    private final VoteStatut statutMetier;
    private final String statutAffichage;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFin;
    private final Long creeParId;
    private final String creeParNom;
    private final Long depenseId;
    private final long totalPour;
    private final long totalContre;
    private final long totalNeutre;
    private final long totalVotants;
    private final long totalVotantsEligibles;
    private final String choixMajoritaire;
    private final boolean currentUserHasVoted;
    private final String currentUserChoice;
    private final String currentUserComment;
    private final boolean currentUserCanVote;
    private final long joursRestants;
    private final boolean finProche;
    private final List<VoteHousingParticipationResponse> participationsLogements;

    public VoteOverviewResponse(
            final Long id,
            final Long residenceId,
            final String titre,
            final String description,
            final BigDecimal montantEstime,
            final VoteStatut statutMetier,
            final String statutAffichage,
            final LocalDateTime dateDebut,
            final LocalDateTime dateFin,
            final Long creeParId,
            final String creeParNom,
            final Long depenseId,
            final long totalPour,
            final long totalContre,
            final long totalNeutre,
            final long totalVotants,
            final long totalVotantsEligibles,
            final String choixMajoritaire,
            final boolean currentUserHasVoted,
            final String currentUserChoice,
            final String currentUserComment,
            final boolean currentUserCanVote,
            final long joursRestants,
            final boolean finProche,
            final List<VoteHousingParticipationResponse> participationsLogements
    ) {
        this.id = id;
        this.residenceId = residenceId;
        this.titre = titre;
        this.description = description;
        this.montantEstime = montantEstime;
        this.statutMetier = statutMetier;
        this.statutAffichage = statutAffichage;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.creeParId = creeParId;
        this.creeParNom = creeParNom;
        this.depenseId = depenseId;
        this.totalPour = totalPour;
        this.totalContre = totalContre;
        this.totalNeutre = totalNeutre;
        this.totalVotants = totalVotants;
        this.totalVotantsEligibles = totalVotantsEligibles;
        this.choixMajoritaire = choixMajoritaire;
        this.currentUserHasVoted = currentUserHasVoted;
        this.currentUserChoice = currentUserChoice;
        this.currentUserComment = currentUserComment;
        this.currentUserCanVote = currentUserCanVote;
        this.joursRestants = joursRestants;
        this.finProche = finProche;
        this.participationsLogements = participationsLogements;
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

    public VoteStatut getStatutMetier() {
        return statutMetier;
    }

    public String getStatutAffichage() {
        return statutAffichage;
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

    public String getCreeParNom() {
        return creeParNom;
    }

    public Long getDepenseId() {
        return depenseId;
    }

    public long getTotalPour() {
        return totalPour;
    }

    public long getTotalContre() {
        return totalContre;
    }

    public long getTotalNeutre() {
        return totalNeutre;
    }

    public long getTotalVotants() {
        return totalVotants;
    }

    public long getTotalVotantsEligibles() {
        return totalVotantsEligibles;
    }

    public String getChoixMajoritaire() {
        return choixMajoritaire;
    }

    public boolean isCurrentUserHasVoted() {
        return currentUserHasVoted;
    }

    public String getCurrentUserChoice() {
        return currentUserChoice;
    }

    public String getCurrentUserComment() {
        return currentUserComment;
    }

    public boolean isCurrentUserCanVote() {
        return currentUserCanVote;
    }

    public long getJoursRestants() {
        return joursRestants;
    }

    public boolean isFinProche() {
        return finProche;
    }

    public List<VoteHousingParticipationResponse> getParticipationsLogements() {
        return participationsLogements;
    }
}
