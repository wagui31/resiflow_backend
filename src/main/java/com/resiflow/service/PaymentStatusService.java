package com.resiflow.service;

import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.PaymentMonth;
import com.resiflow.entity.PaymentMonthStatus;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import com.resiflow.repository.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentStatusService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PaiementRepository paiementRepository;
    private final PaymentMonthRepository paymentMonthRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentStatusService(
            final PaiementRepository paiementRepository,
            final PaymentMonthRepository paymentMonthRepository,
            final UserRepository userRepository
    ) {
        this.paiementRepository = paiementRepository;
        this.paymentMonthRepository = paymentMonthRepository;
        this.userRepository = userRepository;
    }

    public PaymentStatusService(final PaiementRepository paiementRepository, final UserRepository userRepository) {
        this(paiementRepository, null, userRepository);
    }

    @Transactional
    public User refreshPaymentStatus(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        StatutPaiement computedStatus = calculateStatus(user);
        if (user.getStatutPaiement() != computedStatus) {
            user.setStatutPaiement(computedStatus);
            userRepository.save(user);
        }
        return user;
    }

    public StatutPaiement calculateStatus(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return StatutPaiement.A_JOUR;
        }
        if (paymentMonthRepository == null) {
            return calculateStatusFromValidatedPayments(user);
        }
        if (user.getDateEntreeResidence() == null) {
            return StatutPaiement.EN_RETARD;
        }

        LocalDate today = LocalDate.now();
        List<PaymentMonth> paymentMonths = paymentMonthRepository.findAllByUser_IdOrderByMonthAsc(user.getId());
        YearMonth currentMonth = YearMonth.from(today);
        for (YearMonth cursor = YearMonth.from(user.getDateEntreeResidence());
             !cursor.isAfter(currentMonth);
             cursor = cursor.plusMonths(1)) {
            String month = cursor.format(MONTH_FORMATTER);
            boolean paid = paymentMonths.stream()
                    .anyMatch(paymentMonth -> paymentMonth.getMonth().equals(month)
                            && paymentMonth.getStatus() == PaymentMonthStatus.PAID);
            if (!paid && cursor.atEndOfMonth().isBefore(today)) {
                return StatutPaiement.EN_RETARD;
            }
        }
        return StatutPaiement.A_JOUR;
    }

    private StatutPaiement calculateStatusFromValidatedPayments(final User user) {
        Paiement lastPayment = paiementRepository.findFirstByUtilisateur_IdAndStatusOrderByDateFinDescDatePaiementDesc(
                        user.getId(),
                        PaiementStatus.VALIDATED
                )
                .orElse(null);
        if (lastPayment == null) {
            return StatutPaiement.EN_RETARD;
        }
        return LocalDate.now().isAfter(lastPayment.getDateFin()) ? StatutPaiement.EN_RETARD : StatutPaiement.A_JOUR;
    }

    public StatutPaiement calculateStatus(final Paiement paiement) {
        if (paiement == null) {
            throw new IllegalArgumentException("Paiement must not be null");
        }
        if (paiement.getStatus() != PaiementStatus.VALIDATED) {
            return StatutPaiement.EN_RETARD;
        }

        return LocalDate.now().isAfter(paiement.getDateFin())
                ? StatutPaiement.EN_RETARD
                : StatutPaiement.A_JOUR;
    }
}
