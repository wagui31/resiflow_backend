package com.resiflow.service;

import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.TransactionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CagnotteServiceTest {

    @Test
    void calculerSoldeIncludesCorrectionsFromTransactionLedger() {
        TransactionCagnotteRepository transactionRepository = mock(TransactionCagnotteRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        CagnotteService service = new CagnotteService(transactionRepository, residenceAccessService);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.ADMIN);

        when(transactionRepository.findAllByResidence_IdOrderByDateCreationDesc(7L)).thenReturn(List.of(
                transaction(TypeTransactionCagnotte.CONTRIBUTION, "21000.00"),
                transaction(TypeTransactionCagnotte.DEPENSE, "12000.00"),
                transaction(TypeTransactionCagnotte.CORRECTION, "4000.00")
        ));

        BigDecimal result = service.calculerSolde(7L, authenticatedUser);

        assertThat(result).isEqualByComparingTo("13000.00");
        verify(residenceAccessService).getResidenceForMember(7L, authenticatedUser);
        verify(transactionRepository).findAllByResidence_IdOrderByDateCreationDesc(7L);
    }

    private TransactionCagnotte transaction(final TypeTransactionCagnotte type, final String montant) {
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setType(type);
        transaction.setMontant(new BigDecimal(montant));
        return transaction;
    }
}
