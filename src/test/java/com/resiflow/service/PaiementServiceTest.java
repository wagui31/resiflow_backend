package com.resiflow.service;

import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.dto.CreateDepensePartagePaiementRequest;
import com.resiflow.entity.Depense;
import com.resiflow.dto.PaymentStatusTimelineResponse;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.PaymentMonth;
import com.resiflow.entity.PaymentMonthStatus;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutDepense;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    void createMyPaiementCreatesPendingPaiementForAuthenticatedUser() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User user = buildUser(10L, 7L);
        when(residenceAccessService.getUserForRead(10L, authenticatedUser)).thenReturn(user);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(user);
        when(paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiement(
                10L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(false);
        when(paymentMonthRepository.findByUser_IdAndMonth(eq(10L), any())).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMyPaiementRequest request = new CreateMyPaiementRequest();
        request.setNombreMois(2);
        request.setDateDebut(LocalDate.of(2026, 4, 1));

        Paiement result = paiementService.createMyPaiement(request, authenticatedUser);

        assertThat(result.getUtilisateur().getId()).isEqualTo(10L);
        assertThat(result.getResidence().getId()).isEqualTo(7L);
        assertThat(result.getNombreMois()).isEqualTo(2);
        assertThat(result.getMontantMensuel()).isEqualByComparingTo("6000.00");
        assertThat(result.getMontantTotal()).isEqualByComparingTo("12000.00");
        assertThat(result.getDateDebut()).isEqualTo(LocalDate.of(2026, 4, 1));
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

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User user = buildUser(10L, 7L);
        when(residenceAccessService.getUserForRead(10L, authenticatedUser)).thenReturn(user);
        when(paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiement(
                10L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(false);
        when(paymentMonthRepository.findByUser_IdAndMonth(10L, "2026-03"))
                .thenReturn(Optional.of(buildPaidMonth(user, "2026-03")));
        when(paymentMonthRepository.findByUser_IdAndMonth(10L, "2026-04"))
                .thenReturn(Optional.empty());

        CreateMyPaiementRequest request = new CreateMyPaiementRequest();
        request.setNombreMois(2);
        request.setDateDebut(LocalDate.of(2026, 3, 1));

        assertThatThrownBy(() -> paiementService.createMyPaiement(request, authenticatedUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment months already paid: 2026-03");

        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void getAdminUserPaymentStatusByEmailRejectsNonActiveResident() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);
        when(userRepository.findByEmailAndResidence_IdAndStatus("pending@example.com", 7L, UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paiementService.getAdminUserPaymentStatusByEmail("pending@example.com", authenticatedUser))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessage("User not found in residence: pending@example.com");
    }

    @Test
    void createAdminUserPaiementByEmailCreatesPendingPaiementForActiveResident() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);
        User adminUser = buildUser(2L, 7L);
        User residentUser = buildUser(10L, 7L);
        residentUser.setEmail("resident@example.com");
        when(userRepository.findByEmailAndResidence_IdAndStatus("resident@example.com", 7L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(residentUser));
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(adminUser);
        when(paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiement(
                10L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(false);
        when(paymentMonthRepository.findByUser_IdAndMonth(eq(10L), any())).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMyPaiementRequest request = new CreateMyPaiementRequest();
        request.setNombreMois(2);
        request.setDateDebut(LocalDate.of(2026, 4, 1));

        Paiement result = paiementService.createAdminUserPaiementByEmail(
                "resident@example.com",
                request,
                authenticatedUser
        );

        assertThat(result.getUtilisateur().getId()).isEqualTo(10L);
        assertThat(result.getResidence().getId()).isEqualTo(7L);
        assertThat(result.getNombreMois()).isEqualTo(2);
        assertThat(result.getMontantMensuel()).isEqualByComparingTo("6000.00");
        assertThat(result.getMontantTotal()).isEqualByComparingTo("12000.00");
        assertThat(result.getDateDebut()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(result.getDateFin()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(result.getStatus()).isEqualTo(PaiementStatus.PENDING);
        assertThat(result.getCreePar().getId()).isEqualTo(2L);
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

        PaiementService paiementService = new PaiementService(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository,
                depenseService
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);
        User user = buildUser(10L, 7L);
        User actor = buildUser(10L, 7L);
        Depense depense = buildSharedDepense(99L, user.getResidence());

        when(residenceAccessService.getUserForRead(10L, authenticatedUser)).thenReturn(user);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(depenseService.getRequiredDepense(99L)).thenReturn(depense);
        when(paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiementAndDepense_Id(
                10L,
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
        assertThat(result.getDateDebut()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(result.getDateFin()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(result.getStatus()).isEqualTo(PaiementStatus.PENDING);
    }

    private User buildUser(final Long userId, final Long residenceId) {
        Residence residence = new Residence();
        residence.setId(residenceId);
        residence.setMontantMensuel(new BigDecimal("6000.00"));

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setResidence(residence);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setStatutPaiement(StatutPaiement.EN_RETARD);
        return user;
    }

    private PaymentMonth buildPaidMonth(final User user, final String month) {
        PaymentMonth paymentMonth = new PaymentMonth();
        paymentMonth.setUser(user);
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
