package com.resiflow.dto;

import com.resiflow.entity.VoteStatut;

public class VoteResultResponse {

    private final Long voteId;
    private final long totalPour;
    private final long totalContre;
    private final long totalNeutre;
    private final VoteStatut statut;

    public VoteResultResponse(
            final Long voteId,
            final long totalPour,
            final long totalContre,
            final long totalNeutre,
            final VoteStatut statut
    ) {
        this.voteId = voteId;
        this.totalPour = totalPour;
        this.totalContre = totalContre;
        this.totalNeutre = totalNeutre;
        this.statut = statut;
    }

    public Long getVoteId() {
        return voteId;
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

    public VoteStatut getStatut() {
        return statut;
    }
}
