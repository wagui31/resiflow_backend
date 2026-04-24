package com.resiflow.service;

import com.resiflow.dto.CreateCorrectionCagnotteRequest;
import com.resiflow.dto.CreateCorrectionCagnotteResponse;
import com.resiflow.entity.CorrectionCagnotte;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.CorrectionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrectionCagnotteServiceTest {

    @Test
    void createCorrectionCreatesPositiveCorrectionTransactionFromTargetBalance() {
        CorrectionCagnotteRepository correctionRepository = mock(CorrectionCagnotteRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);

        CorrectionCagnotteService service = new CorrectionCagnotteService(
                correctionRepository,
                residenceAccessService,
                cagnotteService,
                transactionCagnotteService
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(9L, "admin@example.com", 7L, UserRole.ADMIN);
        Residence residence = new Residence();
        residence.setId(7L);
        User actor = new User();
        actor.setId(9L);

        when(residenceAccessService.getResidenceForAdmin(7L, authenticatedUser)).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(cagnotteService.calculerSoldeInterne(7L)).thenReturn(new BigDecimal("100.00"));
        when(correctionRepository.save(any(CorrectionCagnotte.class))).thenAnswer(invocation -> {
            CorrectionCagnotte correction = invocation.getArgument(0);
            correction.setId(42L);
            return correction;
        });
        when(transactionCagnotteService.createCorrectionTransaction(any(CorrectionCagnotte.class))).thenAnswer(invocation -> {
            CorrectionCagnotte correction = invocation.getArgument(0);
            TransactionCagnotte transaction = new TransactionCagnotte();
            transaction.setId(99L);
            transaction.setType(TypeTransactionCagnotte.CORRECTION);
            transaction.setMontant(correction.getDelta());
            transaction.setDateCreation(correction.getDateCreation());
            return transaction;
        });

        CreateCorrectionCagnotteRequest request = new CreateCorrectionCagnotteRequest();
        request.setNouveauSolde(new BigDecimal("130.00"));
        request.setMotif("Ajustement manuel");

        CreateCorrectionCagnotteResponse response = service.createCorrection(7L, request, authenticatedUser);

        assertThat(response.getResidenceId()).isEqualTo(7L);
        assertThat(response.getAncienSolde()).isEqualByComparingTo("100.00");
        assertThat(response.getNouveauSolde()).isEqualByComparingTo("130.00");
        assertThat(response.getDelta()).isEqualByComparingTo("30.00");
        assertThat(response.getCorrectionId()).isEqualTo(42L);
        assertThat(response.getTransactionId()).isEqualTo(99L);
        assertThat(response.getTypeTransaction()).isEqualTo(TypeTransactionCagnotte.CORRECTION);
        verify(transactionCagnotteService).createCorrectionTransaction(any(CorrectionCagnotte.class));
    }

    @Test
    void createCorrectionSupportsNegativeDeltaWhileTargetBalanceStaysNonNegative() {
        CorrectionCagnotteRepository correctionRepository = mock(CorrectionCagnotteRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);

        CorrectionCagnotteService service = new CorrectionCagnotteService(
                correctionRepository,
                residenceAccessService,
                cagnotteService,
                transactionCagnotteService
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(9L, "admin@example.com", 7L, UserRole.ADMIN);
        Residence residence = new Residence();
        residence.setId(7L);
        User actor = new User();
        actor.setId(9L);

        when(residenceAccessService.getResidenceForAdmin(7L, authenticatedUser)).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(cagnotteService.calculerSoldeInterne(7L)).thenReturn(new BigDecimal("100.00"));
        when(correctionRepository.save(any(CorrectionCagnotte.class))).thenAnswer(invocation -> {
            CorrectionCagnotte correction = invocation.getArgument(0);
            correction.setId(43L);
            return correction;
        });
        when(transactionCagnotteService.createCorrectionTransaction(any(CorrectionCagnotte.class))).thenAnswer(invocation -> {
            CorrectionCagnotte correction = invocation.getArgument(0);
            TransactionCagnotte transaction = new TransactionCagnotte();
            transaction.setId(100L);
            transaction.setType(TypeTransactionCagnotte.CORRECTION);
            transaction.setMontant(correction.getDelta());
            transaction.setDateCreation(correction.getDateCreation());
            return transaction;
        });

        CreateCorrectionCagnotteRequest request = new CreateCorrectionCagnotteRequest();
        request.setNouveauSolde(new BigDecimal("70.00"));
        request.setMotif("Retrait erreur");

        CreateCorrectionCagnotteResponse response = service.createCorrection(7L, request, authenticatedUser);

        assertThat(response.getDelta()).isEqualByComparingTo("-30.00");
    }

    @Test
    void createCorrectionRejectsZeroDelta() {
        CorrectionCagnotteRepository correctionRepository = mock(CorrectionCagnotteRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);

        CorrectionCagnotteService service = new CorrectionCagnotteService(
                correctionRepository,
                residenceAccessService,
                cagnotteService,
                transactionCagnotteService
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(9L, "admin@example.com", 7L, UserRole.ADMIN);
        Residence residence = new Residence();
        residence.setId(7L);
        User actor = new User();
        actor.setId(9L);

        when(residenceAccessService.getResidenceForAdmin(7L, authenticatedUser)).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(cagnotteService.calculerSoldeInterne(7L)).thenReturn(new BigDecimal("100.00"));

        CreateCorrectionCagnotteRequest request = new CreateCorrectionCagnotteRequest();
        request.setNouveauSolde(new BigDecimal("100.00"));
        request.setMotif("Sans changement");

        assertThatThrownBy(() -> service.createCorrection(7L, request, authenticatedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correction delta must not be zero");
    }

    @Test
    void createCorrectionRejectsNegativeTargetBalance() {
        CorrectionCagnotteRepository correctionRepository = mock(CorrectionCagnotteRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);

        CorrectionCagnotteService service = new CorrectionCagnotteService(
                correctionRepository,
                residenceAccessService,
                cagnotteService,
                transactionCagnotteService
        );

        CreateCorrectionCagnotteRequest request = new CreateCorrectionCagnotteRequest();
        request.setNouveauSolde(new BigDecimal("-1.00"));
        request.setMotif("Invalide");

        assertThatThrownBy(() -> service.createCorrection(7L, request, new AuthenticatedUser(9L, "admin@example.com", 7L, UserRole.ADMIN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nouveau solde must be greater than or equal to zero");
    }
}
