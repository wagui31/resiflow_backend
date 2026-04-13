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

    List<Paiement> findAllByLogement_IdOrderByDatePaiementDesc(Long logementId);

    List<Paiement> findAllByResidence_IdOrderByDatePaiementDesc(Long residenceId);

    List<Paiement> findAllByResidence_IdAndTypePaiementOrderByDatePaiementDesc(Long residenceId, TypePaiement typePaiement);

    Optional<Paiement> findFirstByLogement_IdOrderByDateFinDescDatePaiementDesc(Long logementId);

    List<Paiement> findAllByLogement_IdAndStatusOrderByDatePaiementDesc(Long logementId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusOrderByDatePaiementDesc(Long residenceId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
            Long residenceId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    @Query("""
            select p
            from Paiement p
            join fetch p.logement l
            join fetch p.residence r
            join fetch p.creePar c
            where r.id = :residenceId
              and p.status = :status
              and p.typePaiement = :typePaiement
            order by p.datePaiement desc
            """)
    List<Paiement> findAllAdminPendingWithDetails(
            @Param("residenceId") Long residenceId,
            @Param("status") PaiementStatus status,
            @Param("typePaiement") TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusOrderByDateFinDescDatePaiementDesc(Long logementId, PaiementStatus status);

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusOrderByDatePaiementDesc(Long logementId, PaiementStatus status);

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementAndDepense_IdOrderByDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    List<Paiement> findAllByDepense_IdOrderByDatePaiementDesc(Long depenseId);

    boolean existsByLogement_IdAndStatus(Long logementId, PaiementStatus status);

    boolean existsByLogement_IdAndStatusAndTypePaiement(Long logementId, PaiementStatus status, TypePaiement typePaiement);

    boolean existsByLogement_IdAndStatusAndTypePaiementAndDepense_Id(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    @Query("""
            select coalesce(sum(p.montantTotal), 0)
            from Paiement p
            where p.logement.id = :logementId
              and p.depense.id = :depenseId
              and p.typePaiement = :typePaiement
              and p.status = :status
            """)
    BigDecimal sumMontantTotalByLogementAndDepenseAndTypeAndStatus(
            @Param("logementId") Long logementId,
            @Param("depenseId") Long depenseId,
            @Param("typePaiement") TypePaiement typePaiement,
            @Param("status") PaiementStatus status
    );

    @Query("""
            select p.logement.id, coalesce(sum(p.montantTotal), 0)
            from Paiement p
            where p.depense.id = :depenseId
              and p.typePaiement = :typePaiement
              and p.status = :status
            group by p.logement.id
            """)
    List<Object[]> sumMontantTotalByDepenseAndTypeAndStatusGroupedByLogement(
            @Param("depenseId") Long depenseId,
            @Param("typePaiement") TypePaiement typePaiement,
            @Param("status") PaiementStatus status
    );
}
