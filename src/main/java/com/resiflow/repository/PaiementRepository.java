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

    List<Paiement> findAllByLogement_IdAndIsDeletedFalseOrderByDatePaiementDesc(Long logementId);

    List<Paiement> findAllByResidence_IdAndIsDeletedFalseOrderByDatePaiementDesc(Long residenceId);

    List<Paiement> findAllByResidence_IdAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
            Long residenceId,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndIsDeletedFalseOrderByDateFinDescDatePaiementDesc(Long logementId);

    List<Paiement> findAllByLogement_IdAndStatusAndIsDeletedFalseOrderByDatePaiementDesc(Long logementId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusAndIsDeletedFalseOrderByDatePaiementDesc(Long residenceId, PaiementStatus status);

    List<Paiement> findAllByResidence_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
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
              and p.isDeleted = false
            order by p.datePaiement desc
            """)
    List<Paiement> findAllAdminPendingWithDetails(
            @Param("residenceId") Long residenceId,
            @Param("status") PaiementStatus status,
            @Param("typePaiement") TypePaiement typePaiement
    );

    @Query("""
            select p
            from Paiement p
            join fetch p.logement l
            join fetch p.residence r
            join fetch p.creePar c
            where p.status = :status
              and p.typePaiement = :typePaiement
              and p.isDeleted = false
            order by p.datePaiement desc
            """)
    List<Paiement> findAllAdminPendingWithDetails(
            @Param("status") PaiementStatus status,
            @Param("typePaiement") TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndIsDeletedFalseOrderByDateFinDescDatePaiementDesc(
            Long logementId,
            PaiementStatus status
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDateFinDescDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndIsDeletedFalseOrderByDatePaiementDesc(
            Long logementId,
            PaiementStatus status
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    Optional<Paiement> findFirstByLogement_IdAndStatusAndTypePaiementAndDepense_IdAndIsDeletedFalseOrderByDatePaiementDesc(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    List<Paiement> findAllByDepense_IdAndIsDeletedFalseOrderByDatePaiementDesc(Long depenseId);

    List<Paiement> findAllByDepense_Id(Long depenseId);

    List<Paiement> findAllByDepense_IdAndLogement_IdAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
            Long depenseId,
            Long logementId,
            TypePaiement typePaiement
    );

    boolean existsByLogement_IdAndStatusAndIsDeletedFalse(Long logementId, PaiementStatus status);

    boolean existsByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalse(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement
    );

    boolean existsByLogement_IdAndStatusAndTypePaiementAndDepense_IdAndIsDeletedFalse(
            Long logementId,
            PaiementStatus status,
            TypePaiement typePaiement,
            Long depenseId
    );

    Optional<Paiement> findByIdAndIsDeletedFalse(Long paiementId);

    @Query("""
            select coalesce(sum(p.montantTotal), 0)
            from Paiement p
            where p.logement.id = :logementId
              and p.depense.id = :depenseId
              and p.typePaiement = :typePaiement
              and p.status = :status
              and p.isDeleted = false
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
              and p.isDeleted = false
            group by p.logement.id
            """)
    List<Object[]> sumMontantTotalByDepenseAndTypeAndStatusGroupedByLogement(
            @Param("depenseId") Long depenseId,
            @Param("typePaiement") TypePaiement typePaiement,
            @Param("status") PaiementStatus status
    );
}
