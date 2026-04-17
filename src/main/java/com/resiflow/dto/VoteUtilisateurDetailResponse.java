package com.resiflow.dto;

import com.resiflow.entity.VoteUtilisateur;
import java.time.LocalDateTime;

public class VoteUtilisateurDetailResponse {

    private final Long userId;
    private final String userEmail;
    private final Long logementId;
    private final String logementCodeInterne;
    private final String choix;
    private final String commentaire;
    private final LocalDateTime dateVote;

    public VoteUtilisateurDetailResponse(
            final Long userId,
            final String userEmail,
            final Long logementId,
            final String logementCodeInterne,
            final String choix,
            final String commentaire,
            final LocalDateTime dateVote
    ) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.logementId = logementId;
        this.logementCodeInterne = logementCodeInterne;
        this.choix = choix;
        this.commentaire = commentaire;
        this.dateVote = dateVote;
    }

    public static VoteUtilisateurDetailResponse fromEntity(final VoteUtilisateur voteUtilisateur) {
        return new VoteUtilisateurDetailResponse(
                voteUtilisateur.getUtilisateur().getId(),
                voteUtilisateur.getUtilisateur().getEmail(),
                voteUtilisateur.getUtilisateur().getLogementId(),
                voteUtilisateur.getUtilisateur().getLogement() == null
                        ? null
                        : voteUtilisateur.getUtilisateur().getLogement().getCodeInterne(),
                voteUtilisateur.getChoix().name(),
                voteUtilisateur.getCommentaire(),
                voteUtilisateur.getDateVote()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getLogementCodeInterne() {
        return logementCodeInterne;
    }

    public String getChoix() {
        return choix;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public LocalDateTime getDateVote() {
        return dateVote;
    }
}
