package com.resiflow.service;

import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.PaymentMonth;
import com.resiflow.entity.PaymentMonthStatus;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentStatusService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PaiementRepository paiementRepository;
    private final PaymentMonthRepository paymentMonthRepository;

    public PaymentStatusService(
            final PaiementRepository paiementRepository,
            final PaymentMonthRepository paymentMonthRepository
    ) {
        this.paiementRepository = paiementRepository;
        this.paymentMonthRepository = paymentMonthRepository;
    }

    public User refreshPaymentStatus(final User user) {
        return user;
    }

    public StatutPaiement calculateStatus(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return StatutPaiement.A_JOUR;
        }
        return calculateStatus(user.getLogement(), user.getDateEntreeResidence());
    }

    public StatutPaiement calculateStatus(final Logement logement) {
        return calculateStatus(logement, logement == null ? null : logement.getDateActivation() == null
                ? null
                : logement.getDateActivation().toLocalDate());
    }

    public StatutPaiement calculateStatus(final Paiement paiement) {
        if (paiement == null) {
            throw new IllegalArgumentException("Paiement must not be null");
        }
        if (paiement.getStatus() != PaiementStatus.VALIDATED) {
            return StatutPaiement.EN_RETARD;
        }
        return LocalDate.now().isAfter(paiement.getDateFin()) ? StatutPaiement.EN_RETARD : StatutPaiement.A_JOUR;
    }

    private StatutPaiement calculateStatus(final Logement logement, final LocalDate startDate) {
        if (logement == null || !Boolean.TRUE.equals(logement.getActive()) || startDate == null) {
            return StatutPaiement.EN_RETARD;
        }

        LocalDate today = LocalDate.now();
        List<PaymentMonth> paymentMonths = paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logement.getId());
        YearMonth currentMonth = YearMonth.from(today);
        for (YearMonth cursor = YearMonth.from(startDate); !cursor.isAfter(currentMonth); cursor = cursor.plusMonths(1)) {
            String month = cursor.format(MONTH_FORMATTER);
            boolean paid = paymentMonths.stream()
                    .anyMatch(paymentMonth -> paymentMonth.getMonth().equals(month)
                            && paymentMonth.getStatus() == PaymentMonthStatus.PAID);
            if (!paid && cursor.atEndOfMonth().isBefore(today)) {
                return StatutPaiement.EN_RETARD;
            }
        }

        Paiement lastPayment = paiementRepository
                .findFirstByLogement_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
                        logement.getId(),
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);
        if (lastPayment == null) {
            return StatutPaiement.EN_RETARD;
        }
        return LocalDate.now().isAfter(lastPayment.getDateFin()) ? StatutPaiement.EN_RETARD : StatutPaiement.A_JOUR;
    }
}
