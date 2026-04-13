package com.resiflow.repository;

import com.resiflow.entity.Logement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogementRepository extends JpaRepository<Logement, Long> {

    List<Logement> findAllByResidence_IdOrderByNumeroAsc(Long residenceId);

    List<Logement> findAllByResidence_IdAndActiveOrderByNumeroAsc(Long residenceId, Boolean active);

    Optional<Logement> findByCodeInterne(String codeInterne);

    long countByResidence_Id(Long residenceId);

    long countByResidence_IdAndActiveTrue(Long residenceId);
}
