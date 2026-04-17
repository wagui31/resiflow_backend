package com.resiflow.dto;

public class VoteHousingParticipationResponse {

    private final Long logementId;
    private final String codeInterne;
    private final long totalEligibleVoters;
    private final long totalVoters;
    private final boolean hasVoted;

    public VoteHousingParticipationResponse(
            final Long logementId,
            final String codeInterne,
            final long totalEligibleVoters,
            final long totalVoters,
            final boolean hasVoted
    ) {
        this.logementId = logementId;
        this.codeInterne = codeInterne;
        this.totalEligibleVoters = totalEligibleVoters;
        this.totalVoters = totalVoters;
        this.hasVoted = hasVoted;
    }

    public Long getLogementId() {
        return logementId;
    }

    public String getCodeInterne() {
        return codeInterne;
    }

    public long getTotalEligibleVoters() {
        return totalEligibleVoters;
    }

    public long getTotalVoters() {
        return totalVoters;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }
}
