package com.resiflow.controller;

import com.resiflow.dto.DepenseResponse;
import com.resiflow.dto.CreateVoteRequest;
import com.resiflow.dto.VoteActionRequest;
import com.resiflow.dto.VoteDetailsResponse;
import com.resiflow.dto.VoteResponse;
import com.resiflow.dto.VoteResultResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.VoteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(final VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VoteResponse> createVote(
            @RequestBody final CreateVoteRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VoteResponse.fromEntity(voteService.createVote(request, authenticatedUser)));
    }

    @GetMapping("/residence/{residenceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VoteResponse>> getVotesByResidence(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(voteService.getVotesByResidence(residenceId, authenticatedUser).stream()
                .map(VoteResponse::fromEntity)
                .toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoteResponse> getVote(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(VoteResponse.fromEntity(voteService.getVote(id, authenticatedUser)));
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VoteResponse> closeVote(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(VoteResponse.fromEntity(voteService.closeVote(id, authenticatedUser)));
    }

    @PostMapping("/{id}/reouvrir")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VoteResponse> reopenVote(
            @PathVariable final Long id,
            @RequestBody final VoteActionRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(VoteResponse.fromEntity(voteService.reopenVote(id, request, authenticatedUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteVote(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        voteService.deleteVote(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/utilisateurs/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VoteResponse> removeUserVote(
            @PathVariable final Long id,
            @PathVariable final Long userId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(VoteResponse.fromEntity(voteService.removeUserVote(id, userId, authenticatedUser)));
    }

    @PostMapping("/{id}/voter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoteResponse> voter(
            @PathVariable final Long id,
            @RequestBody final VoteActionRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(VoteResponse.fromEntity(voteService.voter(id, request, authenticatedUser)));
    }

    @GetMapping("/{id}/resultat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoteResultResponse> getVoteResult(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(voteService.getVoteResult(id, authenticatedUser));
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoteDetailsResponse> getVoteDetails(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(voteService.getVoteDetails(id, authenticatedUser));
    }

    @PostMapping("/{id}/creer-depense")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepenseResponse> createDepenseFromVote(
            @PathVariable final Long id,
            @RequestBody(required = false) final VoteActionRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voteService.createDepenseFromVote(id, request, authenticatedUser));
    }
}
