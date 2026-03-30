package com.resiflow.controller;

import com.resiflow.dto.CategorieDepenseResponse;
import com.resiflow.dto.CreateCategorieDepenseRequest;
import com.resiflow.service.CategorieDepenseService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/categories-depenses", "/categories-depenses"})
public class CategorieDepenseController {

    private final CategorieDepenseService categorieDepenseService;

    public CategorieDepenseController(final CategorieDepenseService categorieDepenseService) {
        this.categorieDepenseService = categorieDepenseService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategorieDepenseResponse>> getCategoriesDepenses() {
        return ResponseEntity.ok(categorieDepenseService.getCategoriesDepenses().stream()
                .map(CategorieDepenseResponse::fromEntity)
                .toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CategorieDepenseResponse> createCategorieDepense(
            @RequestBody final CreateCategorieDepenseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategorieDepenseResponse.fromEntity(categorieDepenseService.createCategorieDepense(request)));
    }
}
