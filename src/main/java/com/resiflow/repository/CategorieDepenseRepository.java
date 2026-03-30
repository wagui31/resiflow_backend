package com.resiflow.repository;

import com.resiflow.entity.CategorieDepense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieDepenseRepository extends JpaRepository<CategorieDepense, Long> {

    boolean existsByNomIgnoreCase(String nom);

    List<CategorieDepense> findAllByOrderByNomAsc();
}
