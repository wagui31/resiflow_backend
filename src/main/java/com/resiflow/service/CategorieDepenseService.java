package com.resiflow.service;

import com.resiflow.dto.CreateCategorieDepenseRequest;
import com.resiflow.entity.CategorieDepense;
import com.resiflow.repository.CategorieDepenseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategorieDepenseService {

    private final CategorieDepenseRepository categorieDepenseRepository;

    public CategorieDepenseService(final CategorieDepenseRepository categorieDepenseRepository) {
        this.categorieDepenseRepository = categorieDepenseRepository;
    }

    @Transactional(readOnly = true)
    public List<CategorieDepense> getCategoriesDepenses() {
        return categorieDepenseRepository.findAllByOrderByNomAsc();
    }

    @Transactional
    public CategorieDepense createCategorieDepense(final CreateCategorieDepenseRequest request) {
        validateCreateRequest(request);
        String nom = request.getNom().trim();
        if (categorieDepenseRepository.existsByNomIgnoreCase(nom)) {
            throw new IllegalArgumentException("Categorie depense already exists");
        }

        CategorieDepense categorieDepense = new CategorieDepense();
        categorieDepense.setNom(nom);
        return categorieDepenseRepository.save(categorieDepense);
    }

    @Transactional(readOnly = true)
    public CategorieDepense getRequiredCategorieDepense(final Long categorieId) {
        if (categorieId == null) {
            throw new IllegalArgumentException("Categorie depense ID must not be null");
        }
        return categorieDepenseRepository.findById(categorieId)
                .orElseThrow(() -> new NoSuchElementException("Categorie depense not found: " + categorieId));
    }

    private void validateCreateRequest(final CreateCategorieDepenseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create categorie depense request must not be null");
        }
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Categorie depense nom must not be blank");
        }
    }
}
