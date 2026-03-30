package com.resiflow.controller;

import com.resiflow.dto.CagnotteSoldeResponse;
import com.resiflow.dto.TransactionCagnotteResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.CagnotteService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cagnotte")
public class CagnotteController {

    private final CagnotteService cagnotteService;

    public CagnotteController(final CagnotteService cagnotteService) {
        this.cagnotteService = cagnotteService;
    }

    @GetMapping("/{residenceId}/solde")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CagnotteSoldeResponse> getSolde(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(new CagnotteSoldeResponse(
                residenceId,
                cagnotteService.calculerSolde(residenceId, authenticatedUser)
        ));
    }

    @GetMapping("/{residenceId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionCagnotteResponse>> getTransactions(
            @PathVariable final Long residenceId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(cagnotteService.getTransactions(residenceId, authenticatedUser).stream()
                .map(TransactionCagnotteResponse::fromEntity)
                .toList());
    }
}
