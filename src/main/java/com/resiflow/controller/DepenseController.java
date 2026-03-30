package com.resiflow.controller;

import com.resiflow.dto.CreateDepenseRequest;
import com.resiflow.dto.DepenseResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DepenseService;
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

    public DepenseController(final DepenseService depenseService) {
        this.depenseService = depenseService;
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
}
