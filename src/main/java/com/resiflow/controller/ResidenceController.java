package com.resiflow.controller;

import com.resiflow.dto.CreateResidenceRequest;
import com.resiflow.dto.DashboardResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.ResidenceResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.entity.Residence;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DashboardService;
import com.resiflow.service.PaiementService;
import com.resiflow.service.ResidenceService;
import com.resiflow.service.StatsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/residences", "/api/residences"})
public class ResidenceController {

    private final ResidenceService residenceService;
    private final DashboardService dashboardService;
    private final PaiementService paiementService;
    private final StatsService statsService;

    public ResidenceController(
            final ResidenceService residenceService,
            final DashboardService dashboardService,
            final PaiementService paiementService,
            final StatsService statsService
    ) {
        this.residenceService = residenceService;
        this.dashboardService = dashboardService;
        this.paiementService = paiementService;
        this.statsService = statsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResidenceResponse> createResidence(@RequestBody final CreateResidenceRequest request) {
        Residence residence = residenceService.createResidence(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResidenceResponse.fromResidence(residence));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<ResidenceResponse>> getResidences() {
        List<ResidenceResponse> responses = residenceService.getAllResidences().stream()
                .map(ResidenceResponse::fromResidence)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{residenceId}/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDashboard(residenceId, authenticatedUser));
    }

    @GetMapping("/{residenceId}/impayes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResidenceImpayeResponse>> getImpayes(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getImpayesByResidence(residenceId, authenticatedUser));
    }

    @GetMapping("/{residenceId}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StatsResponse> getStats(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(statsService.getStats(residenceId, authenticatedUser));
    }

    @PutMapping("/{residenceId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResidenceResponse> updateResidence(
            @PathVariable final Long residenceId,
            @RequestBody final CreateResidenceRequest request
    ) {
        Residence residence = residenceService.updateResidence(residenceId, request);
        return ResponseEntity.ok(ResidenceResponse.fromResidence(residence));
    }

    @DeleteMapping("/{residenceId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteResidence(@PathVariable final Long residenceId) {
        residenceService.deleteResidence(residenceId);
        return ResponseEntity.noContent().build();
    }
}
