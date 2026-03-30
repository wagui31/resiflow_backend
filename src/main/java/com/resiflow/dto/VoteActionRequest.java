package com.resiflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoteActionRequest {

    private String choix;
    private String commentaire;
    private LocalDateTime nouvelleDateFin;
    private BigDecimal montant;

    public String getChoix() {
        return choix;
    }

    public void setChoix(final String choix) {
        this.choix = choix;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(final String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getNouvelleDateFin() {
        return nouvelleDateFin;
    }

    public void setNouvelleDateFin(final LocalDateTime nouvelleDateFin) {
        this.nouvelleDateFin = nouvelleDateFin;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(final BigDecimal montant) {
        this.montant = montant;
    }
}
