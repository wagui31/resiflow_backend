package com.resiflow.repository;

import com.resiflow.entity.CorrectionCagnotte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionCagnotteRepository extends JpaRepository<CorrectionCagnotte, Long> {

    List<CorrectionCagnotte> findAllByResidence_IdOrderByDateCreationDesc(Long residenceId);
}
