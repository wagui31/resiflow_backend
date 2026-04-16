package com.resiflow.service;

import com.resiflow.dto.CreateAdminDepensePartagePaiementRequest;
import com.resiflow.dto.CreateDepensePartagePaiementRequest;
import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.LogementSummaryResponse;
import com.resiflow.dto.PaymentHistoryItemResponse;
import com.resiflow.dto.PaymentStatusMonthResponse;
import com.resiflow.dto.PaymentStatusTimelineResponse;
import com.resiflow.dto.PendingPaymentResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.UserPaiementHistoryResponse;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Logement;
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
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.PaymentMonthRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaiementService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PaiementRepository paiementRepository;
    private final ResidenceAccessService residenceAccessService;
    private final PaymentStatusService paymentStatusService;
    private final TransactionCagnotteService transactionCagnotteService;
    private final UserRepository userRepository;
    private final PaymentMonthRepository paymentMonthRepository;
    private final DepenseService depenseService;
    private final LogementService logementService;
    private final LogementRepository logementRepository;

    public PaiementService(
            final PaiementRepository paiementRepository,
            final ResidenceAccessService residenceAccessService,
            final PaymentStatusService paymentStatusService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository,
            final PaymentMonthRepository paymentMonthRepository,
            final DepenseService depenseService,
            final LogementService logementService,
            final LogementRepository logementRepository
    ) {
        this.paiementRepository = paiementRepository;
        this.residenceAccessService = residenceAccessService;
        this.paymentStatusService = paymentStatusService;
        this.transactionCagnotteService = transactionCagnotteService;
        this.userRepository = userRepository;
        this.paymentMonthRepository = paymentMonthRepository;
        this.depenseService = depenseService;
        this.logementService = logementService;
        this.logementRepository = logementRepository;
    }

    @Transactional
    public Paiement createPaiement(final CreatePaiementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);
        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        Logement logement = logementService.getRequiredLogement(request.getLogementId());
        logementService.ensureLogementBelongsToResidence(logement.getId(), residence.getId());
        return createCagnottePaiementForLogement(logement, residence, request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createMyPaiement(final CreateMyPaiementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateMyRequest(request);
        Logement logement = getCurrentUserLogement(authenticatedUser);
        Residence residence = logement.getResidence();
        return createCagnottePaiementForLogement(logement, residence, request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createAdminUserPaiementByEmail(
            final String email,
            final CreateMyPaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateMyRequest(request);
        User user = getAdminTargetUserByEmail(email, authenticatedUser);
        return createCagnottePaiementForLogement(user.getLogement(), user.getResidence(), request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createMyDepensePartagePaiement(
            final Long depenseId,
            final CreateDepensePartagePaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateDepensePartageRequest(request);
        return createDepensePartagePaiement(depenseId, getCurrentUserLogement(authenticatedUser), request.getMontant(), authenticatedUser, false);
    }

    @Transactional
    public Paiement createAdminDepensePartagePaiement(
            final Long depenseId,
            final CreateAdminDepensePartagePaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateAdminDepensePartageRequest(request);
        Logement logement = logementService.getRequiredLogement(request.getLogementId());
        return createDepensePartagePaiement(depenseId, logement, request.getMontant(), authenticatedUser, true);
    }

    @Transactional
    public Paiement validatePaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = getPaiementForAdminAction(paiementId, authenticatedUser);
        if (paiement.getStatus() != PaiementStatus.PENDING) {
            throw new IllegalStateException("Only pending paiements can be validated");
        }
        paiement.setStatus(PaiementStatus.VALIDATED);
        Paiement savedPaiement = paiementRepository.save(paiement);
        if (savedPaiement.getTypePaiement() == TypePaiement.CAGNOTTE) {
            transactionCagnotteService.createContributionTransaction(savedPaiement);
            syncPaymentMonths(savedPaiement);
        }
        return savedPaiement;
    }

    @Transactional
    public Paiement validateSharedExpensePaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = getSharedExpensePaiementForAdminAction(paiementId, authenticatedUser);
        if (paiement.getStatus() != PaiementStatus.PENDING) {
            throw new IllegalStateException("Only pending paiements can be validated");
        }
        paiement.setStatus(PaiementStatus.VALIDATED);
        return paiementRepository.save(paiement);
    }

    @Transactional
    public Paiement rejectPaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = getPaiementForAdminAction(paiementId, authenticatedUser);
        if (paiement.getStatus() != PaiementStatus.PENDING) {
            throw new IllegalStateException("Only pending paiements can be rejected");
        }
        paiement.setStatus(PaiementStatus.REJECTED);
        return paiementRepository.save(paiement);
    }

    @Transactional
    public Paiement rejectSharedExpensePaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = getSharedExpensePaiementForAdminAction(paiementId, authenticatedUser);
        if (paiement.getStatus() != PaiementStatus.PENDING) {
            throw new IllegalStateException("Only pending paiements can be rejected");
        }
        paiement.setStatus(PaiementStatus.REJECTED);
        return paiementRepository.save(paiement);
    }

    @Transactional
    public void deletePendingPaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new NoSuchElementException("Paiement not found: " + paiementId));
        ensureDeletePendingAllowed(paiement, authenticatedUser);
        if (paiement.getStatus() != PaiementStatus.PENDING) {
            throw new IllegalStateException("Only pending paiements can be deleted");
        }
        paiementRepository.delete(paiement);
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByUtilisateur(final Long userId, final AuthenticatedUser authenticatedUser) {
        User user = residenceAccessService.getUserForRead(userId, authenticatedUser);
        return paiementRepository.findAllByLogement_IdOrderByDatePaiementDesc(user.getLogementId()).stream()
                .filter(paiement -> paiement.getTypePaiement() == TypePaiement.CAGNOTTE)
                .toList();
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
                        paiement.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForAdmin(residenceId, authenticatedUser);
        return paiementRepository.findAllByResidence_IdAndTypePaiementOrderByDatePaiementDesc(residenceId, TypePaiement.CAGNOTTE);
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByDepense(final Long depenseId, final AuthenticatedUser authenticatedUser) {
        Depense depense = requireSharedDepense(depenseId);
        residenceAccessService.ensureMemberAccessToResidence(authenticatedUser, depense.getResidence().getId());
        return paiementRepository.findAllByDepense_IdOrderByDatePaiementDesc(depenseId);
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPendingPaiementsForAdmin(final AuthenticatedUser authenticatedUser) {
        ensureAdmin(authenticatedUser);
        residenceAccessService.getResidenceForAdmin(authenticatedUser.residenceId(), authenticatedUser);
        return paiementRepository.findAllAdminPendingWithDetails(
                authenticatedUser.residenceId(),
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        );
    }

    @Transactional(readOnly = true)
    public List<Paiement> getPendingSharedExpensePaiementsForAdmin(final AuthenticatedUser authenticatedUser) {
        ensureAdmin(authenticatedUser);
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return paiementRepository.findAllAdminPendingWithDetails(
                    PaiementStatus.PENDING,
                    TypePaiement.DEPENSE_PARTAGE
            );
        }
        residenceAccessService.getResidenceForAdmin(authenticatedUser.residenceId(), authenticatedUser);
        return paiementRepository.findAllAdminPendingWithDetails(
                authenticatedUser.residenceId(),
                PaiementStatus.PENDING,
                TypePaiement.DEPENSE_PARTAGE
        );
    }

    @Transactional(readOnly = true)
    public List<ResidenceImpayeResponse> getImpayesByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        LocalDate today = LocalDate.now();
        return logementRepository.findAllByResidence_IdOrderByNumeroAsc(residenceId).stream()
                .filter(logement -> paymentStatusService.calculateStatus(logement) == StatutPaiement.EN_RETARD)
                .map(logement -> toResidenceImpayeResponse(logement, today))
                .sorted(Comparator.comparing(ResidenceImpayeResponse::getNombreJoursRetard,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Long countResidentsEnRetard(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return logementRepository.findAllByResidence_IdOrderByNumeroAsc(residenceId).stream()
                .filter(logement -> paymentStatusService.calculateStatus(logement) == StatutPaiement.EN_RETARD)
                .count();
    }

    @Transactional(readOnly = true)
    public PaymentStatusTimelineResponse getMyPaymentStatus(final AuthenticatedUser authenticatedUser) {
        return buildPaymentStatusTimeline(getCurrentUserLogement(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public PaymentStatusTimelineResponse getAdminUserPaymentStatusByEmail(
            final String email,
            final AuthenticatedUser authenticatedUser
    ) {
        return buildPaymentStatusTimeline(getAdminTargetUserByEmail(email, authenticatedUser).getLogement());
    }

    @Transactional(readOnly = true)
    public PaymentStatusTimelineResponse getAdminLogementPaymentStatus(
            final Long logementId,
            final AuthenticatedUser authenticatedUser
    ) {
        ensureAdmin(authenticatedUser);
        Logement logement = logementService.getRequiredLogement(logementId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, logement.getResidenceId());
        return buildPaymentStatusTimeline(logement);
    }

    private PaymentStatusTimelineResponse buildPaymentStatusTimeline(final Logement logement) {
        LocalDate startDate = logement.getDateActivation() == null ? null : logement.getDateActivation().toLocalDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth currentMonth = YearMonth.from(today);
        Map<String, PaymentMonth> paymentMonthByMonth = getPaymentMonthsByMonth(logement.getId());

        List<PaymentStatusMonthResponse> months = new ArrayList<>();
        boolean hasOverdue = false;
        for (YearMonth cursor = startMonth; !cursor.isAfter(currentMonth); cursor = cursor.plusMonths(1)) {
            String month = cursor.format(MONTH_FORMATTER);
            PaymentMonth paymentMonth = paymentMonthByMonth.get(month);
            boolean paid = paymentMonth != null && paymentMonth.getStatus() == PaymentMonthStatus.PAID;
            if (!paid && cursor.atEndOfMonth().isBefore(today)) {
                hasOverdue = true;
            }
            months.add(new PaymentStatusMonthResponse(month, paid));
        }

        Paiement lastValidatedPaiement = paiementRepository
                .findFirstByLogement_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
                        logement.getId(),
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);
        Paiement pendingPaiement = paiementRepository
                .findFirstByLogement_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                        logement.getId(),
                        PaiementStatus.PENDING,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);

        LocalDate dateFin = lastValidatedPaiement == null ? null : lastValidatedPaiement.getDateFin();
        boolean nextDueWarning = dateFin != null && !dateFin.isBefore(today) && !dateFin.isAfter(today.plusDays(15));

        return new PaymentStatusTimelineResponse(
                hasOverdue ? "OVERDUE" : "UP_TO_DATE",
                dateFin,
                nextDueWarning,
                pendingPaiement == null ? null : new PendingPaymentResponse(
                        pendingPaiement.getId(),
                        pendingPaiement.getMontantTotal(),
                        pendingPaiement.getNombreMois(),
                        pendingPaiement.getDateDebut(),
                        pendingPaiement.getDateFin()
                ),
                months,
                paiementRepository.findAllByLogement_IdAndStatusOrderByDatePaiementDesc(logement.getId(), PaiementStatus.VALIDATED)
                        .stream()
                        .filter(paiement -> paiement.getTypePaiement() == TypePaiement.CAGNOTTE)
                        .map(this::toHistoryItem)
                        .toList()
        );
    }

    private PaymentHistoryItemResponse toHistoryItem(final Paiement paiement) {
        return new PaymentHistoryItemResponse(
                paiement.getDatePaiement().toLocalDate(),
                paiement.getMontantTotal(),
                formatMonth(paiement.getDateDebut()) + " - " + formatMonth(paiement.getDateFin())
        );
    }

    private Map<String, PaymentMonth> getPaymentMonthsByMonth(final Long logementId) {
        Map<String, PaymentMonth> result = new HashMap<>();
        for (PaymentMonth paymentMonth : paymentMonthRepository.findAllByLogement_IdOrderByMonthAsc(logementId)) {
            result.put(paymentMonth.getMonth(), paymentMonth);
        }
        return result;
    }

    private void syncPaymentMonths(final Paiement paiement) {
        if (paiement.getTypePaiement() != TypePaiement.CAGNOTTE) {
            return;
        }
        for (String month : getCoveredMonths(paiement.getDateDebut(), paiement.getDateFin())) {
            PaymentMonth paymentMonth = paymentMonthRepository.findByLogement_IdAndMonth(paiement.getLogementId(), month)
                    .orElseGet(PaymentMonth::new);
            paymentMonth.setLogement(paiement.getLogement());
            paymentMonth.setMonth(month);
            paymentMonth.setStatus(PaymentMonthStatus.PAID);
            paymentMonth.setPayment(paiement);
            paymentMonthRepository.save(paymentMonth);
        }
    }

    private List<String> getCoveredMonths(final LocalDate dateDebut, final LocalDate dateFin) {
        List<String> months = new ArrayList<>();
        for (YearMonth cursor = YearMonth.from(dateDebut); !cursor.isAfter(YearMonth.from(dateFin)); cursor = cursor.plusMonths(1)) {
            months.add(cursor.format(MONTH_FORMATTER));
        }
        return months;
    }

    private Paiement getPaiementForAdminAction(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new NoSuchElementException("Paiement not found: " + paiementId));
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, paiement.getResidence().getId());
        return paiement;
    }

    private Paiement getSharedExpensePaiementForAdminAction(final Long paiementId, final AuthenticatedUser authenticatedUser) {
        Paiement paiement = getPaiementForAdminAction(paiementId, authenticatedUser);
        if (paiement.getTypePaiement() != TypePaiement.DEPENSE_PARTAGE) {
            throw new IllegalStateException("Paiement must be a shared expense paiement");
        }
        return paiement;
    }

    private void ensureDeletePendingAllowed(final Paiement paiement, final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return;
        }
        if (authenticatedUser.role() == UserRole.ADMIN) {
            residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, paiement.getResidence().getId());
            return;
        }
        User actor = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authenticatedUser.userId()));
        if (!actor.getLogementId().equals(paiement.getLogementId())) {
            throw new AccessDeniedException("Cannot delete another logement pending paiement");
        }
    }

    private void validateCreateRequest(final CreatePaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create paiement request must not be null");
        }
        if (request.getLogementId() == null) {
            throw new IllegalArgumentException("Logement ID must not be null");
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
        if (request.getDateDebut().getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Date debut must be the first day of a month");
        }
    }

    private void validateCreateMyRequest(final CreateMyPaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create my paiement request must not be null");
        }
        if (request.getNombreMois() == null || request.getNombreMois() <= 0) {
            throw new IllegalArgumentException("Nombre de mois must be greater than zero");
        }
        if (request.getDateDebut() == null) {
            throw new IllegalArgumentException("Date debut must not be null");
        }
        if (request.getDateDebut().getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Date debut must be the first day of a month");
        }
    }

    private void validateCreateDepensePartageRequest(final CreateDepensePartagePaiementRequest request) {
        if (request == null || request.getMontant() == null || request.getMontant().signum() <= 0) {
            throw new IllegalArgumentException("Montant must be greater than zero");
        }
    }

    private void validateCreateAdminDepensePartageRequest(final CreateAdminDepensePartagePaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create admin depense partage paiement request must not be null");
        }
        if (request.getLogementId() == null) {
            throw new IllegalArgumentException("Logement ID must not be null");
        }
        if (request.getMontant() == null || request.getMontant().signum() <= 0) {
            throw new IllegalArgumentException("Montant must be greater than zero");
        }
    }

    private ResidenceImpayeResponse toResidenceImpayeResponse(final Logement logement, final LocalDate today) {
        Paiement lastPayment = paiementRepository
                .findFirstByLogement_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
                        logement.getId(),
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);
        LocalDate dateFinDernierPaiement = lastPayment == null ? null : lastPayment.getDateFin();
        Long nombreJoursRetard = dateFinDernierPaiement == null ? null : ChronoUnit.DAYS.between(dateFinDernierPaiement, today);
        return new ResidenceImpayeResponse(
                logement.getId(),
                LogementSummaryResponse.fromEntity(logement),
                dateFinDernierPaiement,
                nombreJoursRetard
        );
    }

    private LocalDate computeDateFin(final LocalDate dateDebut, final Integer nombreMois) {
        return dateDebut.plusMonths(nombreMois).minusDays(1);
    }

    private String formatMonth(final LocalDate date) {
        return date == null ? null : YearMonth.from(date).format(MONTH_FORMATTER);
    }

    private Paiement createCagnottePaiementForLogement(
            final Logement logement,
            final Residence residence,
            final Integer nombreMois,
            final LocalDate dateDebut,
            final AuthenticatedUser authenticatedUser
    ) {
        if (!residence.getId().equals(logement.getResidenceId())) {
            throw new IllegalArgumentException("Logement does not belong to the selected residence");
        }
        if (paiementRepository.existsByLogement_IdAndStatusAndTypePaiement(
                logement.getId(),
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )) {
            throw new IllegalStateException("Logement already has a pending paiement");
        }
        ensureNoPaidMonthOverlap(logement.getId(), dateDebut, nombreMois);

        Paiement paiement = new Paiement();
        paiement.setLogement(logement);
        paiement.setResidence(residence);
        paiement.setNombreMois(nombreMois);
        paiement.setMontantMensuel(residence.getMontantMensuel());
        paiement.setMontantTotal(residence.getMontantMensuel().multiply(BigDecimal.valueOf(nombreMois)));
        paiement.setDateDebut(dateDebut);
        paiement.setDateFin(computeDateFin(dateDebut, nombreMois));
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setCreePar(residenceAccessService.getRequiredActor(authenticatedUser));
        paiement.setStatus(PaiementStatus.PENDING);
        paiement.setTypePaiement(TypePaiement.CAGNOTTE);
        paiement.setDepense(null);
        return paiementRepository.save(paiement);
    }

    private Paiement createDepensePartagePaiement(
            final Long depenseId,
            final Logement logement,
            final BigDecimal montant,
            final AuthenticatedUser authenticatedUser,
            final boolean autoValidate
    ) {
        Depense depense = requireSharedDepense(depenseId);
        validateSharedPaymentLogement(logement, depense);
        ensureNoPendingSharedPayment(logement.getId(), depense.getId());

        Paiement paiement = new Paiement();
        paiement.setLogement(logement);
        paiement.setResidence(depense.getResidence());
        paiement.setNombreMois(null);
        paiement.setMontantMensuel(montant);
        paiement.setMontantTotal(montant);
        paiement.setDateDebut(depense.getDateCreation() == null ? LocalDate.now() : depense.getDateCreation().toLocalDate());
        paiement.setDateFin(paiement.getDateDebut());
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setCreePar(residenceAccessService.getRequiredActor(authenticatedUser));
        paiement.setStatus(autoValidate ? PaiementStatus.VALIDATED : PaiementStatus.PENDING);
        paiement.setTypePaiement(TypePaiement.DEPENSE_PARTAGE);
        paiement.setDepense(depense);
        return paiementRepository.save(paiement);
    }

    private Depense requireSharedDepense(final Long depenseId) {
        Depense depense = depenseService.getRequiredDepense(depenseId);
        if (depense.getTypeDepense() != TypeDepense.PARTAGE) {
            throw new IllegalStateException("Depense must be of type PARTAGE");
        }
        if (depense.getStatut() != StatutDepense.APPROUVEE) {
            throw new IllegalStateException("Depense must be approved before receiving payments");
        }
        return depense;
    }

    private void validateSharedPaymentLogement(final Logement logement, final Depense depense) {
        if (logement.getResidenceId() == null || !logement.getResidenceId().equals(depense.getResidence().getId())) {
            throw new IllegalArgumentException("Logement does not belong to the expense residence");
        }
    }

    private void ensureNoPendingSharedPayment(final Long logementId, final Long depenseId) {
        if (paiementRepository.existsByLogement_IdAndStatusAndTypePaiementAndDepense_Id(
                logementId,
                PaiementStatus.PENDING,
                TypePaiement.DEPENSE_PARTAGE,
                depenseId
        )) {
            throw new IllegalStateException("Logement already has a pending payment for this expense");
        }
    }

    private void ensureNoPaidMonthOverlap(final Long logementId, final LocalDate dateDebut, final Integer nombreMois) {
        List<String> overlappingMonths = getCoveredMonths(dateDebut, computeDateFin(dateDebut, nombreMois)).stream()
                .filter(month -> paymentMonthRepository.findByLogement_IdAndMonth(logementId, month)
                        .filter(paymentMonth -> paymentMonth.getStatus() == PaymentMonthStatus.PAID)
                        .isPresent())
                .toList();
        if (!overlappingMonths.isEmpty()) {
            throw new IllegalStateException("Payment months already paid: " + String.join(", ", overlappingMonths));
        }
    }

    private User getAdminTargetUserByEmail(final String email, final AuthenticatedUser authenticatedUser) {
        ensureAdmin(authenticatedUser);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        String normalizedEmail = email.trim();
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return userRepository.findByEmailAndStatus(normalizedEmail, UserStatus.ACTIVE)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + normalizedEmail));
        }
        return userRepository.findByEmailAndResidence_IdAndStatus(normalizedEmail, authenticatedUser.residenceId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + normalizedEmail));
    }

    private void ensureAdmin(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() != UserRole.ADMIN && authenticatedUser.role() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Admin role is required for this operation");
        }
        if (authenticatedUser.role() != UserRole.SUPER_ADMIN && authenticatedUser.residenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
    }

    private Logement getCurrentUserLogement(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        User user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authenticatedUser.userId()));
        if (user.getLogement() == null) {
            throw new IllegalStateException("Authenticated user is not assigned to a logement");
        }
        return user.getLogement();
    }
}
