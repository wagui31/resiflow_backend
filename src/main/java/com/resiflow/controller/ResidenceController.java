package com.resiflow.controller;

import com.resiflow.dto.CreateResidenceRequest;
import com.resiflow.dto.DashboardResponse;
import com.resiflow.dto.ResidenceExpenseCategoryStatsResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.ResidencePaymentHousingStatsResponse;
import com.resiflow.dto.ResidenceParticipantsCountResponse;
import com.resiflow.dto.ResidenceResponse;
import com.resiflow.dto.ResidenceViewResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.entity.Residence;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DashboardService;
import com.resiflow.service.PaiementService;
import com.resiflow.service.ResidenceService;
import com.resiflow.service.ResidenceViewService;
import com.resiflow.service.StatsService;
import com.resiflow.service.DepenseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/residences", "/api/residences"})
public class ResidenceController {

    private final ResidenceService residenceService;
    private final DashboardService dashboardService;
    private final PaiementService paiementService;
    private final StatsService statsService;
    private final DepenseService depenseService;
    private final ResidenceViewService residenceViewService;

    public ResidenceController(
            final ResidenceService residenceService,
            final DashboardService dashboardService,
            final PaiementService paiementService,
            final StatsService statsService,
            final DepenseService depenseService,
            final ResidenceViewService residenceViewService
    ) {
        this.residenceService = residenceService;
        this.dashboardService = dashboardService;
        this.paiementService = paiementService;
        this.statsService = statsService;
        this.depenseService = depenseService;
        this.residenceViewService = residenceViewService;
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

    @GetMapping("/{residenceId}/housing-view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResidenceViewResponse> getHousingView(
            @PathVariable final Long residenceId,
            @RequestParam(required = false, name = "q") final String search,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(residenceViewService.getResidenceView(residenceId, search, authenticatedUser));
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

    @GetMapping("/{residenceId}/stats/paiements-logements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResidencePaymentHousingStatsResponse> getPaiementsLogementsStats(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(statsService.getPaiementLogementStats(residenceId, authenticatedUser));
    }

    @GetMapping("/{residenceId}/stats/depenses-par-categorie")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResidenceExpenseCategoryStatsResponse> getDepensesParCategorieStats(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(statsService.getDepenseCategoryStats(residenceId, authenticatedUser));
    }

    @GetMapping("/{residenceId}/participants-actifs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResidenceParticipantsCountResponse> getParticipantsActifs(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(new ResidenceParticipantsCountResponse(
                residenceId,
                depenseService.countActiveParticipants(residenceId, authenticatedUser)
        ));
    }

    @GetMapping("/{residenceId}/logements-participants-actifs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResidenceParticipantsCountResponse> getLogementsParticipantsActifs(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(new ResidenceParticipantsCountResponse(
                residenceId,
                depenseService.countActiveParticipants(residenceId, authenticatedUser)
        ));
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
