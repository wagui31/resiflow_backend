package com.resiflow.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "app.votes.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class VoteScheduler {

    private final VoteService voteService;

    public VoteScheduler(final VoteService voteService) {
        this.voteService = voteService;
    }

    @Scheduled(fixedDelay = 60000)
    public void closeExpiredVotes() {
        voteService.closeExpiredVotes();
    }
}
