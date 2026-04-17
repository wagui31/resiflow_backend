package com.resiflow.service;

import com.resiflow.dto.CreateDepensePartagePaiementRequest;
import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.PaymentMonth;
import com.resiflow.entity.PaymentMonthStatus;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutDepense;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaiementServiceTest {

    @Test
    void createMyPaiementCreatesPendingPaiementForAuthenticatedUserLogement() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        LogementService logementService = mock(LogementService.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository,
                null,
                logementService,
                logementRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User actor = buildUser(10L, 7L, 70L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(actor));
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(paiementRepository.existsByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalse(
                70L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(false);
        when(paymentMonthRepository.findByLogement_IdAndMonth(eq(70L), any())).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMyPaiementRequest request = new CreateMyPaiementRequest();
        request.setNombreMois(2);
        request.setDateDebut(LocalDate.of(2026, 4, 1));

        Paiement result = paiementService.createMyPaiement(request, authenticatedUser);

        assertThat(result.getLogement().getId()).isEqualTo(70L);
        assertThat(result.getResidence().getId()).isEqualTo(7L);
        assertThat(result.getNombreMois()).isEqualTo(2);
        assertThat(result.getDateFin()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(result.getStatus()).isEqualTo(PaiementStatus.PENDING);
        assertThat(result.getCreePar().getId()).isEqualTo(10L);
    }

    @Test
    void createMyPaiementRejectsOverlapWithAlreadyPaidMonths() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        LogementService logementService = mock(LogementService.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository,
                null,
                logementService,
                logementRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User actor = buildUser(10L, 7L, 70L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(actor));
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(paiementRepository.existsByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalse(
                70L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(false);
        when(paymentMonthRepository.findByLogement_IdAndMonth(70L, "2026-03"))
                .thenReturn(Optional.of(buildPaidMonth(70L, "2026-03")));

        CreateMyPaiementRequest request = new CreateMyPaiementRequest();
        request.setNombreMois(2);
        request.setDateDebut(LocalDate.of(2026, 3, 1));

        assertThatThrownBy(() -> paiementService.createMyPaiement(request, authenticatedUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment months already paid: 2026-03");

        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void createMyDepensePartagePaiementLeavesNombreMoisNull() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        DepenseService depenseService = mock(DepenseService.class);
        LogementService logementService = mock(LogementService.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository,
                depenseService,
                logementService,
                logementRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User actor = buildUser(10L, 7L, 70L);
        Depense depense = buildSharedDepense(99L, actor.getResidence());

        when(userRepository.findById(10L)).thenReturn(Optional.of(actor));
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(depenseService.getRequiredDepense(99L)).thenReturn(depense);
        when(paiementRepository.existsByLogement_IdAndStatusAndTypePaiementAndDepense_IdAndIsDeletedFalse(
                70L,
                PaiementStatus.PENDING,
                TypePaiement.DEPENSE_PARTAGE,
                99L
        )).thenReturn(false);
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateDepensePartagePaiementRequest request = new CreateDepensePartagePaiementRequest();
        request.setMontant(new BigDecimal("1500.00"));

        Paiement result = paiementService.createMyDepensePartagePaiement(99L, request, authenticatedUser);

        assertThat(result.getTypePaiement()).isEqualTo(TypePaiement.DEPENSE_PARTAGE);
        assertThat(result.getNombreMois()).isNull();
        assertThat(result.getMontantMensuel()).isEqualByComparingTo("1500.00");
        assertThat(result.getMontantTotal()).isEqualByComparingTo("1500.00");
        assertThat(result.getLogement().getId()).isEqualTo(70L);
        assertThat(result.getStatus()).isEqualTo(PaiementStatus.PENDING);
    }

    private User buildUser(final Long userId, final Long residenceId, final Long logementId) {
        Residence residence = new Residence();
        residence.setId(residenceId);
        residence.setMontantMensuel(new BigDecimal("6000.00"));

        Logement logement = new Logement();
        logement.setId(logementId);
        logement.setResidence(residence);
        logement.setNumero("A101");
        logement.setTypeLogement(TypeLogement.APPARTEMENT);
        logement.setActive(Boolean.TRUE);
        logement.setDateActivation(LocalDateTime.of(2026, 1, 1, 10, 0));

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setResidence(residence);
        user.setLogement(logement);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setDateEntreeResidence(LocalDate.of(2026, 1, 1));
        return user;
    }

    private PaymentMonth buildPaidMonth(final Long logementId, final String month) {
        Logement logement = new Logement();
        logement.setId(logementId);

        PaymentMonth paymentMonth = new PaymentMonth();
        paymentMonth.setLogement(logement);
        paymentMonth.setMonth(month);
        paymentMonth.setStatus(PaymentMonthStatus.PAID);
        return paymentMonth;
    }

    private Depense buildSharedDepense(final Long depenseId, final Residence residence) {
        Depense depense = new Depense();
        depense.setId(depenseId);
        depense.setResidence(residence);
        depense.setTypeDepense(TypeDepense.PARTAGE);
        depense.setStatut(StatutDepense.APPROUVEE);
        depense.setDateCreation(LocalDateTime.of(2026, 4, 1, 9, 0));
        return depense;
    }
}
