package com.resiflow.dto;

import com.resiflow.entity.VoteUtilisateur;
import java.time.LocalDateTime;

public class VoteUtilisateurDetailResponse {

    private final Long userId;
    private final String userEmail;
    private final String choix;
    private final String commentaire;
    private final LocalDateTime dateVote;

    public VoteUtilisateurDetailResponse(
            final Long userId,
            final String userEmail,
            final String choix,
            final String commentaire,
            final LocalDateTime dateVote
    ) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.choix = choix;
        this.commentaire = commentaire;
        this.dateVote = dateVote;
    }

    public static VoteUtilisateurDetailResponse fromEntity(final VoteUtilisateur voteUtilisateur) {
        return new VoteUtilisateurDetailResponse(
                voteUtilisateur.getUtilisateur().getId(),
                voteUtilisateur.getUtilisateur().getEmail(),
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
