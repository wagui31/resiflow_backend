package com.resiflow.service;

import com.resiflow.dto.ResidenceViewResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResidenceViewServiceTest {

    @Test
    void getResidenceViewHandlesNullSortFields() {
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        LogementRepository logementRepository = mock(LogementRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);

        ResidenceViewService service = new ResidenceViewService(
                residenceAccessService,
                logementRepository,
                userRepository,
                paiementRepository,
                cagnotteService,
                paymentStatusService
        );

        Residence residence = new Residence();
        residence.setId(7L);
        residence.setMaxOccupantsParLogement(3);

        Logement currentLogement = buildLogement(10L, residence, "A101", null, "RES7-APPARTEMENT-A101", true);
        Logement secondLogement = buildLogement(11L, residence, "A102", "BAT-A", "RES7-APPARTEMENT-BAT-A-A102", true);

        User actor = buildUser(100L, residence, currentLogement, "actor@example.com", "Actor", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
        User residentWithMissingName = buildUser(
                101L,
                residence,
                currentLogement,
                "resident@example.com",
                "Resident",
                null,
                UserRole.USER,
                UserStatus.ACTIVE
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(100L, "actor@example.com", 7L, UserRole.ADMIN);

        when(residenceAccessService.getResidenceForMember(7L, authenticatedUser)).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(logementRepository.findAllByResidence_IdOrderByNumeroAsc(7L)).thenReturn(List.of(secondLogement, currentLogement));
        when(userRepository.findAllByResidence_IdAndStatusAndRoleIn(eq(7L), eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(actor, residentWithMissingName));
        when(userRepository.findAllByResidence_IdAndStatusAndRoleIn(eq(7L), eq(UserStatus.PENDING), any()))
                .thenReturn(List.of());
        when(paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                7L,
                PaiementStatus.VALIDATED,
                TypePaiement.CAGNOTTE
        )).thenReturn(List.of());
        when(paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                7L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(List.of());
        when(cagnotteService.calculerSolde(7L, authenticatedUser)).thenReturn(BigDecimal.ZERO);
        when(paymentStatusService.calculateStatus(any(Logement.class))).thenReturn(StatutPaiement.A_JOUR);
        when(paymentStatusService.getOverdueMonths(any(Logement.class))).thenReturn(List.of());

        ResidenceViewResponse response = service.getResidenceView(7L, null, authenticatedUser);

        assertThat(response.getLogements()).hasSize(2);
        assertThat(response.getLogements().get(0).getLogement().getId()).isEqualTo(10L);
        assertThat(response.getLogements().get(0).getResidents()).extracting("email")
                .containsExactly("actor@example.com", "resident@example.com");
    }

    @Test
    void getResidenceViewIncludesOverdueMonthsInPaymentPayload() {
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        LogementRepository logementRepository = mock(LogementRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        CagnotteService cagnotteService = mock(CagnotteService.class);
        PaymentStatusService paymentStatusService = mock(PaymentStatusService.class);

        ResidenceViewService service = new ResidenceViewService(
                residenceAccessService,
                logementRepository,
                userRepository,
                paiementRepository,
                cagnotteService,
                paymentStatusService
        );

        Residence residence = new Residence();
        residence.setId(7L);
        residence.setMaxOccupantsParLogement(3);

        Logement logement = buildLogement(10L, residence, "A101", "BAT-A", "RES7-APPARTEMENT-BAT-A-A101", true);
        User actor = buildUser(100L, residence, logement, "actor@example.com", "Actor", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(100L, "actor@example.com", 7L, UserRole.ADMIN);

        when(residenceAccessService.getResidenceForMember(7L, authenticatedUser)).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(authenticatedUser)).thenReturn(actor);
        when(logementRepository.findAllByResidence_IdOrderByNumeroAsc(7L)).thenReturn(List.of(logement));
        when(userRepository.findAllByResidence_IdAndStatusAndRoleIn(eq(7L), eq(UserStatus.ACTIVE), any()))
                .thenReturn(List.of(actor));
        when(userRepository.findAllByResidence_IdAndStatusAndRoleIn(eq(7L), eq(UserStatus.PENDING), any()))
                .thenReturn(List.of());
        when(paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                7L,
                PaiementStatus.VALIDATED,
                TypePaiement.CAGNOTTE
        )).thenReturn(List.of());
        when(paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                7L,
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )).thenReturn(List.of());
        when(cagnotteService.calculerSolde(7L, authenticatedUser)).thenReturn(BigDecimal.ZERO);
        when(paymentStatusService.calculateStatus(logement)).thenReturn(StatutPaiement.EN_RETARD);
        when(paymentStatusService.getOverdueMonths(logement)).thenReturn(List.of("2026-02", "2026-03"));

        ResidenceViewResponse response = service.getResidenceView(7L, null, authenticatedUser);

        assertThat(response.getLogements()).hasSize(1);
        assertThat(response.getLogements().get(0).getPayment().getStatus()).isEqualTo("EN_RETARD");
        assertThat(response.getLogements().get(0).getPayment().getOverdueMonths()).containsExactly("2026-02", "2026-03");
    }

    private Logement buildLogement(
            final Long id,
            final Residence residence,
            final String numero,
            final String immeuble,
            final String codeInterne,
            final boolean active
    ) {
        Logement logement = new Logement();
        logement.setId(id);
        logement.setResidence(residence);
        logement.setTypeLogement(TypeLogement.APPARTEMENT);
        logement.setNumero(numero);
        logement.setImmeuble(immeuble);
        logement.setCodeInterne(codeInterne);
        logement.setActive(active);
        return logement;
    }

    private User buildUser(
            final Long id,
            final Residence residence,
            final Logement logement,
            final String email,
            final String firstName,
            final String lastName,
            final UserRole role,
            final UserStatus status
    ) {
        User user = new User();
        user.setId(id);
        user.setResidence(residence);
        user.setLogement(logement);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("secret");
        user.setRole(role);
        user.setStatus(status);
        user.setDateEntreeResidence(LocalDate.of(2026, 1, 1));
        return user;
    }
}
