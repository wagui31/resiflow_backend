package com.resiflow.repository;

import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findAllByUtilisateur_IdOrderByDatePaiementDesc(Long userId);

    List<Paiement> findAllByResidence_IdOrderByDatePaiementDesc(Long residenceId);

    Optional<Paiement> findFirstByUtilisateur_IdOrderByDateFinDescDatePaiementDesc(Long userId);

    List<Paiement> findAllByUtilisateur_IdAndStatusOrderByDatePaiementDesc(Long userId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusOrderByDatePaiementDesc(Long residenceId, PaiementStatus status);

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusOrderByDateFinDescDatePaiementDesc(Long userId, PaiementStatus status);

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusOrderByDatePaiementDesc(Long userId, PaiementStatus status);

    boolean existsByUtilisateur_IdAndStatus(Long userId, PaiementStatus status);
}
