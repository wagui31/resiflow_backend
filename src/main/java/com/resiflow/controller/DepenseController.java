package com.resiflow.controller;

import com.resiflow.dto.CreateDepenseRequest;
import com.resiflow.dto.CreateAdminDepensePartagePaiementRequest;
import com.resiflow.dto.CreateDepensePartagePaiementRequest;
import com.resiflow.dto.DepenseContributionUserResponse;
import com.resiflow.dto.DepenseResponse;
import com.resiflow.dto.PaiementResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DepenseService;
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
@RequestMapping("/api/depenses")
public class DepenseController {

    private final DepenseService depenseService;
    private final PaiementService paiementService;

    public DepenseController(final DepenseService depenseService, final PaiementService paiementService) {
        this.depenseService = depenseService;
        this.paiementService = paiementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepenseResponse> createDepense(
            @RequestBody final CreateDepenseRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DepenseResponse.fromEntity(depenseService.createDepense(request, authenticatedUser)));
    }

    @PostMapping("/{id}/approuver")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepenseResponse> approuverDepense(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(DepenseResponse.fromEntity(depenseService.approuverDepense(id, authenticatedUser)));
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepenseResponse> rejeterDepense(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(DepenseResponse.fromEntity(depenseService.rejeterDepense(id, authenticatedUser)));
    }

    @GetMapping("/residence/{residenceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepenseResponse>> getDepensesByResidence(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(depenseService.getDepensesByResidence(residenceId, authenticatedUser).stream()
                .map(DepenseResponse::fromEntity)
                .toList());
    }

    @GetMapping("/residence/{residenceId}/cagnotte/approuvees")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepenseResponse>> getApprovedCagnotteDepensesByResidence(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(depenseService.getApprovedCagnotteDepensesByResidence(residenceId, authenticatedUser)
                .stream()
                .map(DepenseResponse::fromEntity)
                .toList());
    }

    @GetMapping("/{id}/contributions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepenseContributionUserResponse>> getDepenseContributions(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(depenseService.getDepenseContributions(id, authenticatedUser));
    }

    @GetMapping("/{id}/paiements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByDepense(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getPaiementsByDepense(id, authenticatedUser).stream()
                .map(PaiementResponse::fromEntity)
                .toList());
    }

    @PostMapping("/{id}/paiements/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> createMyDepensePartagePaiement(
            @PathVariable final Long id,
            @RequestBody final CreateDepensePartagePaiementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaiementResponse.fromEntity(
                        paiementService.createMyDepensePartagePaiement(id, request, authenticatedUser)
                ));
    }

    @PostMapping("/{id}/paiements/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> createAdminDepensePartagePaiement(
            @PathVariable final Long id,
            @RequestBody final CreateAdminDepensePartagePaiementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaiementResponse.fromEntity(
                        paiementService.createAdminDepensePartagePaiement(id, request, authenticatedUser)
                ));
    }
}
