package com.resiflow.service;

import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.PaymentMonth;
import com.resiflow.entity.PaymentMonthStatus;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.TypePaiement;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentStatusServiceTest {

    @Test
    void calculateStatusReturnsOverdueWhenPastMonthsAreMissingEvenIfValidatedCoverageIsStillInFuture() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        PaymentStatusService service = new PaymentStatusService(paiementRepository, paymentMonthRepository);

        LocalDate today = LocalDate.now();
        Logement logement = buildActiveLogement(12L, today.minusMonths(4).atStartOfDay());
        Paiement validatedPaiement = buildValidatedPaiement(logement, today.withDayOfMonth(1), today.plusMonths(2).withDayOfMonth(1).minusDays(1));
        PaymentMonth oldPaidMonth = buildPaymentMonth(logement, today.minusMonths(4).withDayOfMonth(1), PaymentMonthStatus.PAID);

        when(paiementRepository.findFirstByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDateFinDescDatePaiementDesc(
                logement.getId(),
                PaiementStatus.VALIDATED,
                TypePaiement.CAGNOTTE
        )).thenReturn(Optional.of(validatedPaiement));
        when(paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logement.getId()))
                .thenReturn(List.of(oldPaidMonth));

        assertThat(service.calculateStatus(logement)).isEqualTo(StatutPaiement.EN_RETARD);
        assertThat(service.getOverdueMonths(logement)).isNotEmpty();
    }

    @Test
    void calculateStatusReturnsOverdueWhenCoverageEndedAndPastMonthIsMissing() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        PaymentStatusService service = new PaymentStatusService(paiementRepository, paymentMonthRepository);

        LocalDate today = LocalDate.now();
        Logement logement = buildActiveLogement(15L, today.minusMonths(3).atStartOfDay());
        Paiement expiredPaiement = buildValidatedPaiement(logement, today.minusMonths(2).withDayOfMonth(1), today.minusMonths(1).withDayOfMonth(1).minusDays(1));

        when(paiementRepository.findFirstByLogement_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDateFinDescDatePaiementDesc(
                logement.getId(),
                PaiementStatus.VALIDATED,
                TypePaiement.CAGNOTTE
        )).thenReturn(Optional.of(expiredPaiement));
        when(paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logement.getId()))
                .thenReturn(List.of());

        assertThat(service.calculateStatus(logement)).isEqualTo(StatutPaiement.EN_RETARD);
    }

    @Test
    void getOverdueMonthsReturnsMissingPastMonthsAndCurrentMonth() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        PaymentStatusService service = new PaymentStatusService(paiementRepository, paymentMonthRepository);

        LocalDate today = LocalDate.now();
        Logement logement = buildActiveLogement(18L, today.minusMonths(3).atStartOfDay());
        PaymentMonth firstPaidMonth = buildPaymentMonth(logement, today.minusMonths(3).withDayOfMonth(1), PaymentMonthStatus.PAID);

        when(paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logement.getId()))
                .thenReturn(List.of(firstPaidMonth));

        List<String> overdueMonths = service.getOverdueMonths(logement);

        assertThat(overdueMonths).contains(today.minusMonths(2).withDayOfMonth(1).toString().substring(0, 7));
        assertThat(overdueMonths).contains(today.minusMonths(1).withDayOfMonth(1).toString().substring(0, 7));
        assertThat(overdueMonths).contains(today.withDayOfMonth(1).toString().substring(0, 7));
    }

    @Test
    void getOverdueMonthsReturnsCurrentMonthWhenItIsTheOnlyUnpaidMonth() {
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        PaymentMonthRepository paymentMonthRepository = mock(PaymentMonthRepository.class);
        PaymentStatusService service = new PaymentStatusService(paiementRepository, paymentMonthRepository);

        LocalDate today = LocalDate.now();
        Logement logement = buildActiveLogement(21L, today.withDayOfMonth(1).atStartOfDay());

        when(paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logement.getId()))
                .thenReturn(List.of());

        List<String> overdueMonths = service.getOverdueMonths(logement);

        assertThat(overdueMonths).containsExactly(today.withDayOfMonth(1).toString().substring(0, 7));
    }

    private Logement buildActiveLogement(final Long id, final LocalDateTime activationDate) {
        Logement logement = new Logement();
        logement.setId(id);
        logement.setActive(true);
        logement.setDateActivation(activationDate);
        return logement;
    }

    private Paiement buildValidatedPaiement(final Logement logement, final LocalDate dateDebut, final LocalDate dateFin) {
        Paiement paiement = new Paiement();
        paiement.setLogement(logement);
        paiement.setStatus(PaiementStatus.VALIDATED);
        paiement.setTypePaiement(TypePaiement.CAGNOTTE);
        paiement.setDateDebut(dateDebut);
        paiement.setDateFin(dateFin);
        paiement.setDatePaiement(LocalDateTime.now());
        return paiement;
    }

    private PaymentMonth buildPaymentMonth(
            final Logement logement,
            final LocalDate monthDate,
            final PaymentMonthStatus status
    ) {
        PaymentMonth paymentMonth = new PaymentMonth();
        paymentMonth.setLogement(logement);
        paymentMonth.setMonth(monthDate.getYear() + "-" + String.format("%02d", monthDate.getMonthValue()));
        paymentMonth.setStatus(status);
        return paymentMonth;
    }
}
