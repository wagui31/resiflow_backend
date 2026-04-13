package com.resiflow.controller;

import com.resiflow.dto.CreateLogementRequest;
import com.resiflow.dto.CreateLogementsBulkRequest;
import com.resiflow.dto.CreateLogementsBulkResponse;
import com.resiflow.dto.LogementOccupancyResponse;
import com.resiflow.dto.LogementResponse;
import com.resiflow.dto.UpdateLogementRequest;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.LogementService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/logements", "/logements"})
public class LogementController {

    private final LogementService logementService;

    public LogementController(final LogementService logementService) {
        this.logementService = logementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LogementResponse> createLogement(
            @RequestBody final CreateLogementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LogementResponse.fromEntity(logementService.createLogement(request, authenticatedUser)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CreateLogementsBulkResponse> createLogementsBulk(
            @RequestBody final CreateLogementsBulkRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(logementService.createLogementsBulk(request, authenticatedUser));
    }

    @GetMapping("/residence/{residenceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LogementResponse>> getLogementsByResidence(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(logementService.getLogementsByResidence(residenceId, authenticatedUser).stream()
                .map(LogementResponse::fromEntity)
                .toList());
    }

    @GetMapping("/{logementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogementResponse> getLogement(
            @PathVariable final Long logementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(LogementResponse.fromEntity(logementService.getLogement(logementId, authenticatedUser)));
    }

    @PutMapping("/{logementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LogementResponse> updateLogement(
            @PathVariable final Long logementId,
            @RequestBody final UpdateLogementRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(LogementResponse.fromEntity(logementService.updateLogement(logementId, request, authenticatedUser)));
    }

    @PutMapping("/{logementId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LogementResponse> activateLogement(
            @PathVariable final Long logementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(LogementResponse.fromEntity(logementService.activateLogement(logementId, authenticatedUser)));
    }

    @PutMapping("/{logementId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LogementResponse> deactivateLogement(
            @PathVariable final Long logementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(LogementResponse.fromEntity(logementService.deactivateLogement(logementId, authenticatedUser)));
    }

    @GetMapping("/{logementId}/occupancy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogementOccupancyResponse> getOccupancy(
            @PathVariable final Long logementId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(logementService.getOccupancy(logementId, authenticatedUser));
    }
}
