package com.resiflow.repository;

import com.resiflow.entity.Depense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    List<Depense> findAllByResidence_IdOrderByDateCreationDesc(Long residenceId);
}
