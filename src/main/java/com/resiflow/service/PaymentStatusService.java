package com.resiflow.service;

import com.resiflow.entity.Paiement;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentStatusService {

    private final PaiementRepository paiementRepository;
    private final UserRepository userRepository;

    public PaymentStatusService(final PaiementRepository paiementRepository, final UserRepository userRepository) {
        this.paiementRepository = paiementRepository;
        this.userRepository = userRepository;
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

        Paiement lastPayment = paiementRepository.findFirstByUtilisateur_IdOrderByDateFinDescDatePaiementDesc(user.getId())
                .orElse(null);
        if (lastPayment == null) {
            return StatutPaiement.EN_RETARD;
        }

        return LocalDate.now().isAfter(lastPayment.getDateFin())
                ? StatutPaiement.EN_RETARD
                : StatutPaiement.A_JOUR;
    }

    public StatutPaiement calculateStatus(final Paiement paiement) {
        if (paiement == null) {
            throw new IllegalArgumentException("Paiement must not be null");
        }

        return LocalDate.now().isAfter(paiement.getDateFin())
                ? StatutPaiement.EN_RETARD
                : StatutPaiement.A_JOUR;
    }
}
