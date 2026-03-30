package com.resiflow.service;

import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.UserPaiementHistoryResponse;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.User;
import com.resiflow.repository.UserRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ResidenceAccessService residenceAccessService;
    private final PaymentStatusService paymentStatusService;
    private final TransactionCagnotteService transactionCagnotteService;
    private final UserRepository userRepository;

    public PaiementService(
            final PaiementRepository paiementRepository,
            final ResidenceAccessService residenceAccessService,
            final PaymentStatusService paymentStatusService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository
    ) {
        this.paiementRepository = paiementRepository;
        this.residenceAccessService = residenceAccessService;
        this.paymentStatusService = paymentStatusService;
        this.transactionCagnotteService = transactionCagnotteService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Paiement createPaiement(final CreatePaiementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);

        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        User utilisateur = residenceAccessService.getUserForRead(request.getUtilisateurId(), authenticatedUser);
        if (!residence.getId().equals(utilisateur.getResidenceId())) {
            throw new IllegalArgumentException("User does not belong to the selected residence");
        }

        Paiement paiement = new Paiement();
        paiement.setUtilisateur(utilisateur);
        paiement.setResidence(residence);
        paiement.setNombreMois(request.getNombreMois());
        paiement.setMontantMensuel(residence.getMontantMensuel());
        paiement.setMontantTotal(residence.getMontantMensuel().multiply(BigDecimal.valueOf(request.getNombreMois())));
        paiement.setDateDebut(request.getDateDebut());
        paiement.setDateFin(request.getDateDebut().plusMonths(request.getNombreMois()));
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setCreePar(residenceAccessService.getRequiredActor(authenticatedUser));

        Paiement savedPaiement = paiementRepository.save(paiement);
        transactionCagnotteService.createContributionTransaction(savedPaiement);
        paymentStatusService.refreshPaymentStatus(utilisateur);
        return savedPaiement;
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByUtilisateur(final Long userId, final AuthenticatedUser authenticatedUser) {
        User user = residenceAccessService.getUserForRead(userId, authenticatedUser);
        return paiementRepository.findAllByUtilisateur_IdOrderByDatePaiementDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<UserPaiementHistoryResponse> getPaiementHistoryByUtilisateur(
            final Long userId,
            final AuthenticatedUser authenticatedUser
    ) {
        return getPaiementsByUtilisateur(userId, authenticatedUser).stream()
                .map(paiement -> new UserPaiementHistoryResponse(
                        paiement.getDateDebut(),
                        paiement.getDateFin(),
                        paiement.getMontantTotal(),
                        paymentStatusService.calculateStatus(paiement)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForAdmin(residenceId, authenticatedUser);
        return paiementRepository.findAllByResidence_IdOrderByDatePaiementDesc(residenceId);
    }

    @Transactional(readOnly = true)
    public List<ResidenceImpayeResponse> getImpayesByResidence(
            final Long residenceId,
            final AuthenticatedUser authenticatedUser
    ) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        LocalDate today = LocalDate.now();

        return userRepository.findAllByResidence_Id(residenceId).stream()
                .filter(user -> paymentStatusService.calculateStatus(user) == StatutPaiement.EN_RETARD)
                .map(user -> toResidenceImpayeResponse(user, today))
                .sorted(Comparator.comparing(ResidenceImpayeResponse::getNombreJoursRetard,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Long countResidentsEnRetard(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return userRepository.findAllByResidence_Id(residenceId).stream()
                .filter(user -> paymentStatusService.calculateStatus(user) == StatutPaiement.EN_RETARD)
                .count();
    }

    private void validateCreateRequest(final CreatePaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create paiement request must not be null");
        }
        if (request.getUtilisateurId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        if (request.getNombreMois() == null || request.getNombreMois() <= 0) {
            throw new IllegalArgumentException("Nombre de mois must be greater than zero");
        }
        if (request.getDateDebut() == null) {
            throw new IllegalArgumentException("Date debut must not be null");
        }
    }

    private ResidenceImpayeResponse toResidenceImpayeResponse(final User user, final LocalDate today) {
        Paiement lastPayment = paiementRepository.findFirstByUtilisateur_IdOrderByDateFinDescDatePaiementDesc(user.getId())
                .orElse(null);
        LocalDate dateFinDernierPaiement = lastPayment == null ? null : lastPayment.getDateFin();
        Long nombreJoursRetard = dateFinDernierPaiement == null ? null : ChronoUnit.DAYS.between(dateFinDernierPaiement, today);

        return new ResidenceImpayeResponse(
                user.getId(),
                user.getEmail(),
                dateFinDernierPaiement,
                nombreJoursRetard
        );
    }
}
