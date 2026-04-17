package com.resiflow.repository;

import com.resiflow.entity.Depense;
import com.resiflow.entity.StatutDepense;
import com.resiflow.entity.TypeDepense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    List<Depense> findAllByResidence_IdAndIsDeletedFalseOrderByDateCreationDesc(Long residenceId);

    List<Depense> findAllByResidence_IdAndTypeDepenseAndStatutAndIsDeletedFalseOrderByDateCreationDesc(
            Long residenceId,
            TypeDepense typeDepense,
            StatutDepense statut
    );

    java.util.Optional<Depense> findByIdAndIsDeletedFalse(Long depenseId);
}
