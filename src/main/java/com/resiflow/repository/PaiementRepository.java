package com.resiflow.repository;

import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.TypePaiement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findAllByUtilisateur_IdOrderByDatePaiementDesc(Long userId);

    List<Paiement> findAllByResidence_IdOrderByDatePaiementDesc(Long residenceId);

    List<Paiement> findAllByResidence_IdAndTypePaiementOrderByDatePaiementDesc(Long residenceId, TypePaiement typePaiement);

    Optional<Paiement> findFirstByUtilisateur_IdOrderByDateFinDescDatePaiementDesc(Long userId);

    List<Paiement> findAllByUtilisateur_IdAndStatusOrderByDatePaiementDesc(Long userId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusOrderByDatePaiementDesc(Long residenceId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
            Long residenceId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusOrderByDateFinDescDatePaiementDesc(Long userId, PaiementStatus status);

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
            Long userId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusOrderByDatePaiementDesc(Long userId, PaiementStatus status);

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
            Long userId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByUtilisateur_IdAndStatusAndTypePaiementAndDepense_IdOrderByDatePaiementDesc(
            Long userId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    List<Paiement> findAllByDepense_IdOrderByDatePaiementDesc(Long depenseId);

    boolean existsByUtilisateur_IdAndStatus(Long userId, PaiementStatus status);

    boolean existsByUtilisateur_IdAndStatusAndTypePaiement(Long userId, PaiementStatus status, TypePaiement typePaiement);

    boolean existsByUtilisateur_IdAndStatusAndTypePaiementAndDepense_Id(
            Long userId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    @Query("""
            select coalesce(sum(p.montantTotal), 0)
            from Paiement p
            where p.utilisateur.id = :userId
              and p.depense.id = :depenseId
              and p.typePaiement = :typePaiement
              and p.status = :status
            """)
    BigDecimal sumMontantTotalByUtilisateurAndDepenseAndTypeAndStatus(
            @Param("userId") Long userId,
            @Param("depenseId") Long depenseId,
            @Param("typePaiement") TypePaiement typePaiement,
            @Param("status") PaiementStatus status
    );

    @Query("""
            select p.utilisateur.id, coalesce(sum(p.montantTotal), 0)
            from Paiement p
            where p.depense.id = :depenseId
              and p.typePaiement = :typePaiement
              and p.status = :status
            group by p.utilisateur.id
            """)
    List<Object[]> sumMontantTotalByDepenseAndTypeAndStatusGroupedByUtilisateur(
            @Param("depenseId") Long depenseId,
            @Param("typePaiement") TypePaiement typePaiement,
            @Param("status") PaiementStatus status
    );
}
