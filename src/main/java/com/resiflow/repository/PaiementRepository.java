package com.resiflow.repository;

import com.resiflow.entity.Paiement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findAllByUtilisateur_IdOrderByDatePaiementDesc(Long userId);

    List<Paiement> findAllByResidence_IdOrderByDatePaiementDesc(Long residenceId);

    Optional<Paiement> findFirstByUtilisateur_IdOrderByDateFinDescDatePaiementDesc(Long userId);
}
