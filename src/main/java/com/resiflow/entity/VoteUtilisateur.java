package com.resiflow.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "vote_utilisateurs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vote_utilisateurs_vote_utilisateur",
                columnNames = {"vote_id", "utilisateur_id"}
        )
)
public class VoteUtilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteChoix choix;

    @Column
    private String commentaire;

    @Column(name = "date_vote", nullable = false)
    private LocalDateTime dateVote;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(final Vote vote) {
        this.vote = vote;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(final User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public VoteChoix getChoix() {
        return choix;
    }

    public void setChoix(final VoteChoix choix) {
        this.choix = choix;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(final String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateVote() {
        return dateVote;
    }

    public void setDateVote(final LocalDateTime dateVote) {
        this.dateVote = dateVote;
    }
}
