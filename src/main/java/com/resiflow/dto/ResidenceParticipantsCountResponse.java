package com.resiflow.dto;

public class ResidenceParticipantsCountResponse {

    private final Long residenceId;
    private final Long participantsCount;

    public ResidenceParticipantsCountResponse(final Long residenceId, final Long participantsCount) {
        this.residenceId = residenceId;
        this.participantsCount = participantsCount;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public Long getParticipantsCount() {
        return participantsCount;
    }
}
