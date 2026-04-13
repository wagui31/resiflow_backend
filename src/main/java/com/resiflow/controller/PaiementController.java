package com.resiflow.controller;

import com.resiflow.dto.PaymentStatusTimelineResponse;
import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.PaiementAdminPendingResponse;
import com.resiflow.dto.PaiementResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PaiementService;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/paiements", "/api/payments"})
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

    @PostMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> createMyPaiement(
            @RequestBody final CreateMyPaiementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaiementResponse.fromEntity(paiementService.createMyPaiement(request, authenticatedUser)));
    }

    @PostMapping("/admin/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> createAdminUserPaiement(
            @RequestParam final String email,
            @RequestBody final CreateMyPaiementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaiementResponse.fromEntity(
                        paiementService.createAdminUserPaiementByEmail(email, request, authenticatedUser)
                ));
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

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PaiementAdminPendingResponse>> getPendingPaiementsForAdmin(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getPendingPaiementsForAdmin(authenticatedUser).stream()
                .map(PaiementAdminPendingResponse::fromEntity)
                .toList());
    }

    @PutMapping("/{paiementId}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> validatePaiement(
            @PathVariable final Long paiementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(PaiementResponse.fromEntity(paiementService.validatePaiement(paiementId, authenticatedUser)));
    }

    @PutMapping("/{paiementId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaiementResponse> rejectPaiement(
            @PathVariable final Long paiementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(PaiementResponse.fromEntity(paiementService.rejectPaiement(paiementId, authenticatedUser)));
    }

    @DeleteMapping("/{paiementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePendingPaiement(
            @PathVariable final Long paiementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        paiementService.deletePendingPaiement(paiementId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentStatusTimelineResponse> getMyPaymentStatus(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getMyPaymentStatus(authenticatedUser));
    }

    @GetMapping("/admin/user/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentStatusTimelineResponse> getAdminUserPaymentStatus(
            @RequestParam final String email,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getAdminUserPaymentStatusByEmail(email, authenticatedUser));
    }

    @GetMapping("/admin/logement/{logementId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentStatusTimelineResponse> getAdminLogementPaymentStatus(
            @PathVariable final Long logementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getAdminLogementPaymentStatus(logementId, authenticatedUser));
    }
}
