package com.resiflow.service;

import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.repository.TransactionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CagnotteService {

    private final TransactionCagnotteRepository transactionCagnotteRepository;
    private final ResidenceAccessService residenceAccessService;

    public CagnotteService(
            final TransactionCagnotteRepository transactionCagnotteRepository,
            final ResidenceAccessService residenceAccessService
    ) {
        this.transactionCagnotteRepository = transactionCagnotteRepository;
        this.residenceAccessService = residenceAccessService;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerSolde(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return calculerSoldeInterne(residenceId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerSoldeInterne(final Long residenceId) {
        if (residenceId == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        return transactionCagnotteRepository.findAllByResidence_IdOrderByDateCreationDesc(residenceId).stream()
                .map(transaction -> applyTransactionSign(transaction.getType(), transaction.getMontant()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<TransactionCagnotte> getTransactions(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return transactionCagnotteRepository.findAllByResidence_IdOrderByDateCreationDesc(residenceId);
    }

    private BigDecimal applyTransactionSign(final TypeTransactionCagnotte type, final BigDecimal montant) {
        if (montant == null) {
            return BigDecimal.ZERO;
        }
        if (type == TypeTransactionCagnotte.DEPENSE) {
            return montant.negate();
        }
        return montant;
    }
}
