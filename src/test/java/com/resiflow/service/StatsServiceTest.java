package com.resiflow.service;

import com.resiflow.dto.StatsResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.DepenseRepository;
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.TransactionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsServiceTest {

    @Test
    void getStatsIncludesCorrectionInCurrentBalanceAndEvolution() {
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        TransactionCagnotteRepository transactionRepository = mock(TransactionCagnotteRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);

        StatsService service = new StatsService(
                residenceAccessService,
                paiementRepository,
                transactionRepository,
                logementRepository,
                depenseRepository,
                paymentStatusService
        );

        when(paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
                any(),
                any(),
                any()
        )).thenReturn(List.<Paiement>of());
        when(transactionRepository.findAllByResidence_IdOrderByDateCreationDesc(7L)).thenReturn(List.of(
                buildTransaction(TypeTransactionCagnotte.CONTRIBUTION, "100.00", LocalDateTime.of(2026, 4, 1, 10, 0)),
                buildTransaction(TypeTransactionCagnotte.DEPENSE, "30.00", LocalDateTime.of(2026, 4, 2, 10, 0)),
                buildTransaction(TypeTransactionCagnotte.CORRECTION, "-20.00", LocalDateTime.of(2026, 4, 3, 10, 0))
        ));

        StatsResponse result = service.getStats(7L, new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN));

        assertThat(result.getTotalContributions()).isEqualByComparingTo("100.00");
        assertThat(result.getTotalDepenses()).isEqualByComparingTo("30.00");
        assertThat(result.getSoldeActuel()).isEqualByComparingTo("50.00");
        assertThat(result.getEvolutionCagnotte()).hasSize(1);
        assertThat(result.getEvolutionCagnotte().get(0).getSolde()).isEqualByComparingTo("50.00");
    }

    private TransactionCagnotte buildTransaction(
            final TypeTransactionCagnotte type,
            final String montant,
            final LocalDateTime dateCreation
    ) {
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setType(type);
        transaction.setMontant(new BigDecimal(montant));
        transaction.setDateCreation(dateCreation);
        transaction.setLogement(new Logement());
        return transaction;
    }
}
