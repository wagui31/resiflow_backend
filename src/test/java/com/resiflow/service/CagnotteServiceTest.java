package com.resiflow.service;

import com.resiflow.entity.UserRole;
import com.resiflow.repository.TransactionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
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

        when(transactionRepository.sumMontantByResidence(7L)).thenReturn(new BigDecimal("75.00"));

        BigDecimal result = service.calculerSolde(7L, authenticatedUser);

        assertThat(result).isEqualByComparingTo("75.00");
        verify(residenceAccessService).getResidenceForMember(7L, authenticatedUser);
        verify(transactionRepository).sumMontantByResidence(7L);
    }
}
