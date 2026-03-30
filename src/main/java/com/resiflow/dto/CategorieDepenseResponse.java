package com.resiflow.dto;

import com.resiflow.entity.CategorieDepense;

public class CategorieDepenseResponse {

    private final Long id;
    private final String nom;

    public CategorieDepenseResponse(final Long id, final String nom) {
        this.id = id;
        this.nom = nom;
    }

    public static CategorieDepenseResponse fromEntity(final CategorieDepense categorieDepense) {
        return new CategorieDepenseResponse(categorieDepense.getId(), categorieDepense.getNom());
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }
}
