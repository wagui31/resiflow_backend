package com.resiflow.controller;

import com.resiflow.dto.PublicRegistrationContextResponse;
import com.resiflow.dto.PublicRegistrationLogementResponse;
import com.resiflow.dto.PublicRegistrationSearchResponse;
import com.resiflow.service.LogementService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicRegistrationController {

    private final LogementService logementService;

    public PublicRegistrationController(final LogementService logementService) {
        this.logementService = logementService;
    }

    @GetMapping("/residences/{residenceCode}/logements")
    public ResponseEntity<List<PublicRegistrationLogementResponse>> getRegistrationLogements(
            @PathVariable final String residenceCode
    ) {
        return ResponseEntity.ok(logementService.getPublicRegistrationLogements(residenceCode));
    }

    @GetMapping("/residences/{residenceCode}/registration-context")
    public ResponseEntity<PublicRegistrationContextResponse> getRegistrationContext(
            @PathVariable final String residenceCode
    ) {
        return ResponseEntity.ok(logementService.getPublicRegistrationContext(residenceCode));
    }

    @GetMapping("/residences/{residenceCode}/logements/search")
    public ResponseEntity<PublicRegistrationSearchResponse> searchRegistrationLogements(
            @PathVariable final String residenceCode,
            @RequestParam(required = false) final String numero,
            @RequestParam(required = false) final String immeuble
    ) {
        return ResponseEntity.ok(logementService.searchPublicRegistrationLogements(residenceCode, numero, immeuble));
    }
}
