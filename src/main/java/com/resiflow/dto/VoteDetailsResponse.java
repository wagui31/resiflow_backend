package com.resiflow.dto;

import java.util.List;

public class VoteDetailsResponse {

    private final Long voteId;
    private final List<VoteUtilisateurDetailResponse> votesUtilisateurs;

    public VoteDetailsResponse(final Long voteId, final List<VoteUtilisateurDetailResponse> votesUtilisateurs) {
        this.voteId = voteId;
        this.votesUtilisateurs = votesUtilisateurs;
    }

    public Long getVoteId() {
        return voteId;
    }

    public List<VoteUtilisateurDetailResponse> getVotesUtilisateurs() {
        return votesUtilisateurs;
    }
}
