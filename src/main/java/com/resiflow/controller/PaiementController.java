package com.resiflow.controller;

import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.PaiementResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PaiementService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(final PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> createPaiement(
            @RequestBody final CreatePaiementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaiementResponse.fromEntity(paiementService.createPaiement(request, authenticatedUser)));
    }

    @GetMapping("/utilisateur/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByUtilisateur(
            @PathVariable final Long userId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getPaiementsByUtilisateur(userId, authenticatedUser).stream()
                .map(PaiementResponse::fromEntity)
                .toList());
    }

    @GetMapping("/residence/{residenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByResidence(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getPaiementsByResidence(residenceId, authenticatedUser).stream()
                .map(PaiementResponse::fromEntity)
                .toList());
    }
}
