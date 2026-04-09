package com.resiflow.service;

import com.resiflow.dto.CreateAdminDepensePartagePaiementRequest;
import com.resiflow.dto.CreateDepensePartagePaiementRequest;
import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.PaymentHistoryItemResponse;
import com.resiflow.dto.PaymentStatusMonthResponse;
import com.resiflow.dto.PaymentStatusTimelineResponse;
import com.resiflow.dto.PendingPaymentResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.UserPaiementHistoryResponse;
import com.resiflow.entity.Depense;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public PaiementService(
            final PaiementRepository paiementRepository,
            final ResidenceAccessService residenceAccessService,
            final PaymentStatusService paymentStatusService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository,
            final PaymentMonthRepository paymentMonthRepository,
            final DepenseService depenseService
    ) {
        this.paiementRepository = paiementRepository;
        this.residenceAccessService = residenceAccessService;
        this.paymentStatusService = paymentStatusService;
        this.transactionCagnotteService = transactionCagnotteService;
        this.userRepository = userRepository;
        this.paymentMonthRepository = paymentMonthRepository;
        this.depenseService = depenseService;
    }

    public PaiementService(
            final PaiementRepository paiementRepository,
            final ResidenceAccessService residenceAccessService,
            final PaymentStatusService paymentStatusService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository
    ) {
        this(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                null,
                null
        );
    }

    public PaiementService(
            final PaiementRepository paiementRepository,
            final ResidenceAccessService residenceAccessService,
            final PaymentStatusService paymentStatusService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository,
            final PaymentMonthRepository paymentMonthRepository
    ) {
        this(
                paiementRepository,
                residenceAccessService,
                paymentStatusService,
                transactionCagnotteService,
                userRepository,
                paymentMonthRepository,
                null
        );
    }

    @Transactional
    public Paiement createPaiement(final CreatePaiementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);

        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        User utilisateur = residenceAccessService.getUserForRead(request.getUtilisateurId(), authenticatedUser);
        return createCagnottePaiementForUser(utilisateur, residence, request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createMyPaiement(final CreateMyPaiementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateMyRequest(request);

        User utilisateur = residenceAccessService.getUserForRead(authenticatedUser.userId(), authenticatedUser);
        Residence residence = utilisateur.getResidence();
        if (residence == null || residence.getId() == null) {
            throw new IllegalStateException("Authenticated user is not assigned to a residence");
        }

        return createCagnottePaiementForUser(utilisateur, residence, request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createAdminUserPaiementByEmail(
            final String email,
            final CreateMyPaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateMyRequest(request);
        User utilisateur = getAdminTargetUserByEmail(email, authenticatedUser);
        Residence residence = utilisateur.getResidence();
        if (residence == null || residence.getId() == null) {
            throw new IllegalStateException("Target user is not assigned to a residence");
        }

        return createCagnottePaiementForUser(utilisateur, residence, request.getNombreMois(), request.getDateDebut(), authenticatedUser);
    }

    @Transactional
    public Paiement createMyDepensePartagePaiement(
            final Long depenseId,
            final CreateDepensePartagePaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateDepensePartageRequest(request);
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }

        User utilisateur = residenceAccessService.getUserForRead(authenticatedUser.userId(), authenticatedUser);
        return createDepensePartagePaiement(depenseId, utilisateur, request.getMontant(), authenticatedUser, false);
    }

    @Transactional
    public Paiement createAdminDepensePartagePaiement(
            final Long depenseId,
            final CreateAdminDepensePartagePaiementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateCreateAdminDepensePartageRequest(request);
        User utilisateur = residenceAccessService.getUserForRead(request.getUtilisateurId(), authenticatedUser);
        return createDepensePartagePaiement(depenseId, utilisateur, request.getMontant(), authenticatedUser, true);
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
            paymentStatusService.refreshPaymentStatus(savedPaiement.getUtilisateur());
        }
        return savedPaiement;
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
        return paiementRepository.findAllByUtilisateur_IdOrderByDatePaiementDesc(user.getId()).stream()
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
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() != UserRole.ADMIN && authenticatedUser.role() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Admin role is required for this operation");
        }
        if (authenticatedUser.residenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }

        residenceAccessService.getResidenceForAdmin(authenticatedUser.residenceId(), authenticatedUser);
        return paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                authenticatedUser.residenceId(),
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        );
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

    @Transactional(readOnly = true)
    public PaymentStatusTimelineResponse getMyPaymentStatus(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        User user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + authenticatedUser.userId()));
        return buildPaymentStatusTimeline(user);
    }

    @Transactional(readOnly = true)
    public PaymentStatusTimelineResponse getAdminUserPaymentStatusByEmail(
            final String email,
            final AuthenticatedUser authenticatedUser
    ) {
        User user = getAdminTargetUserByEmail(email, authenticatedUser);
        return buildPaymentStatusTimeline(user);
    }

    private User getAdminTargetUserByEmail(final String email, final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() != UserRole.ADMIN && authenticatedUser.role() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Admin role is required for this operation");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        String normalizedEmail = email.trim();
        User user;
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            user = userRepository.findByEmailAndStatus(normalizedEmail, UserStatus.ACTIVE)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + normalizedEmail));
        } else {
            if (authenticatedUser.residenceId() == null) {
                throw new IllegalArgumentException("Residence ID must not be null");
            }
            user = userRepository.findByEmailAndResidence_IdAndStatus(
                            normalizedEmail,
                            authenticatedUser.residenceId(),
                            UserStatus.ACTIVE
                    )
                    .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + normalizedEmail));
        }
        return user;
    }

    private PaymentStatusTimelineResponse buildPaymentStatusTimeline(final User user) {
        if (user.getDateEntreeResidence() == null) {
            throw new IllegalStateException("User residence entry date is missing");
        }

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(user.getDateEntreeResidence());
        YearMonth currentMonth = YearMonth.from(today);
        Map<String, PaymentMonth> paymentMonthByMonth = getPaymentMonthsByMonth(user.getId());

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
                .findFirstByUtilisateur_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
                        user.getId(),
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);
        Paiement pendingPaiement = paiementRepository
                .findFirstByUtilisateur_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                        user.getId(),
                        PaiementStatus.PENDING,
                        TypePaiement.CAGNOTTE
                )
                .orElse(null);

        LocalDate dateFin = lastValidatedPaiement == null ? null : lastValidatedPaiement.getDateFin();
        boolean nextDueWarning = dateFin != null
                && !dateFin.isBefore(today)
                && !dateFin.isAfter(today.plusDays(15));

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
                paiementRepository.findAllByUtilisateur_IdAndStatusOrderByDatePaiementDesc(user.getId(), PaiementStatus.VALIDATED)
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

    private Map<String, PaymentMonth> getPaymentMonthsByMonth(final Long userId) {
        if (paymentMonthRepository == null) {
            return Map.of();
        }

        Map<String, PaymentMonth> result = new HashMap<>();
        for (PaymentMonth paymentMonth : paymentMonthRepository.findAllByUser_IdOrderByMonthAsc(userId)) {
            result.put(paymentMonth.getMonth(), paymentMonth);
        }
        return result;
    }

    private void syncPaymentMonths(final Paiement paiement) {
        if (paymentMonthRepository == null || paiement.getTypePaiement() != TypePaiement.CAGNOTTE) {
            return;
        }

        for (String month : getCoveredMonths(paiement.getDateDebut(), paiement.getDateFin())) {
            PaymentMonth paymentMonth = paymentMonthRepository.findByUser_IdAndMonth(paiement.getUtilisateur().getId(), month)
                    .orElseGet(PaymentMonth::new);
            paymentMonth.setUser(paiement.getUtilisateur());
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
        if (!authenticatedUser.userId().equals(paiement.getUtilisateur().getId())) {
            throw new AccessDeniedException("Cannot delete another user pending paiement");
        }
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
        if (request.getDateDebut().getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Date debut must be the first day of a month");
        }
    }

    private void validateCreateMyRequest(final CreateMyPaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create my paiement request must not be null");
        }

        CreatePaiementRequest delegatedRequest = new CreatePaiementRequest();
        delegatedRequest.setNombreMois(request.getNombreMois());
        delegatedRequest.setDateDebut(request.getDateDebut());
        delegatedRequest.setUtilisateurId(-1L);
        delegatedRequest.setResidenceId(-1L);
        validateCreateRequest(delegatedRequest);
    }

    private void validateCreateDepensePartageRequest(final CreateDepensePartagePaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create depense partage paiement request must not be null");
        }
        if (request.getMontant() == null || request.getMontant().signum() <= 0) {
            throw new IllegalArgumentException("Montant must be greater than zero");
        }
    }

    private void validateCreateAdminDepensePartageRequest(final CreateAdminDepensePartagePaiementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create admin depense partage paiement request must not be null");
        }
        if (request.getUtilisateurId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (request.getMontant() == null || request.getMontant().signum() <= 0) {
            throw new IllegalArgumentException("Montant must be greater than zero");
        }
    }

    private ResidenceImpayeResponse toResidenceImpayeResponse(final User user, final LocalDate today) {
        Paiement lastPayment = paiementRepository
                .findFirstByUtilisateur_IdAndStatusAndTypePaiementOrderByDateFinDescDatePaiementDesc(
                        user.getId(),
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                )
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

    private LocalDate computeDateFin(final LocalDate dateDebut, final Integer nombreMois) {
        return dateDebut.plusMonths(nombreMois).minusDays(1);
    }

    private String formatMonth(final LocalDate date) {
        return date == null ? null : YearMonth.from(date).format(MONTH_FORMATTER);
    }

    private Paiement createCagnottePaiementForUser(
            final User utilisateur,
            final Residence residence,
            final Integer nombreMois,
            final LocalDate dateDebut,
            final AuthenticatedUser authenticatedUser
    ) {
        if (!residence.getId().equals(utilisateur.getResidenceId())) {
            throw new IllegalArgumentException("User does not belong to the selected residence");
        }
        if (paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiement(
                utilisateur.getId(),
                PaiementStatus.PENDING,
                TypePaiement.CAGNOTTE
        )) {
            throw new IllegalStateException("User already has a pending paiement");
        }

        ensureNoPaidMonthOverlap(utilisateur.getId(), dateDebut, nombreMois);

        Paiement paiement = new Paiement();
        paiement.setUtilisateur(utilisateur);
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
            final User utilisateur,
            final BigDecimal montant,
            final AuthenticatedUser authenticatedUser,
            final boolean autoValidate
    ) {
        Depense depense = requireSharedDepense(depenseId);
        validateSharedPaymentUser(utilisateur, depense);
        ensureNoPendingSharedPayment(utilisateur.getId(), depense.getId());

        Paiement paiement = new Paiement();
        paiement.setUtilisateur(utilisateur);
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
        if (depenseService == null) {
            throw new IllegalStateException("Depense service is not configured");
        }
        Depense depense = depenseService.getRequiredDepense(depenseId);
        if (depense.getTypeDepense() != TypeDepense.PARTAGE) {
            throw new IllegalStateException("Depense must be of type PARTAGE");
        }
        if (depense.getStatut() != StatutDepense.APPROUVEE) {
            throw new IllegalStateException("Depense must be approved before receiving payments");
        }
        return depense;
    }

    private void validateSharedPaymentUser(final User utilisateur, final Depense depense) {
        if (utilisateur.getResidenceId() == null || !utilisateur.getResidenceId().equals(depense.getResidence().getId())) {
            throw new IllegalArgumentException("User does not belong to the expense residence");
        }
        if (utilisateur.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only active users can pay a shared expense");
        }
        if (utilisateur.getRole() != UserRole.ADMIN && utilisateur.getRole() != UserRole.USER) {
            throw new IllegalStateException("Only residence members can pay a shared expense");
        }
    }

    private void ensureNoPendingSharedPayment(final Long userId, final Long depenseId) {
        if (paiementRepository.existsByUtilisateur_IdAndStatusAndTypePaiementAndDepense_Id(
                userId,
                PaiementStatus.PENDING,
                TypePaiement.DEPENSE_PARTAGE,
                depenseId
        )) {
            throw new IllegalStateException("User already has a pending payment for this expense");
        }
    }

    private void ensureNoPaidMonthOverlap(final Long userId, final LocalDate dateDebut, final Integer nombreMois) {
        if (paymentMonthRepository == null) {
            return;
        }

        List<String> overlappingMonths = getCoveredMonths(dateDebut, computeDateFin(dateDebut, nombreMois)).stream()
                .filter(month -> paymentMonthRepository.findByUser_IdAndMonth(userId, month)
                        .filter(paymentMonth -> paymentMonth.getStatus() == PaymentMonthStatus.PAID)
                        .isPresent())
                .toList();

        if (!overlappingMonths.isEmpty()) {
            throw new IllegalStateException("Payment months already paid: " + String.join(", ", overlappingMonths));
        }
    }
}
