package com.resiflow.controller;

import com.resiflow.dto.PublicRegistrationLogementResponse;
import com.resiflow.service.LogementService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
