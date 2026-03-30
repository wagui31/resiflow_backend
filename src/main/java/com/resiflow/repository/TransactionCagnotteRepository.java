package com.resiflow.repository;

import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionCagnotteRepository extends JpaRepository<TransactionCagnotte, Long> {

    List<TransactionCagnotte> findAllByResidence_IdOrderByDateCreationDesc(Long residenceId);

    @Query("""
            select coalesce(sum(t.montant), 0)
            from TransactionCagnotte t
            where t.residence.id = :residenceId
              and t.type = :type
            """)
    BigDecimal sumMontantByResidenceAndType(
            @Param("residenceId") Long residenceId,
            @Param("type") TypeTransactionCagnotte type
    );
}
