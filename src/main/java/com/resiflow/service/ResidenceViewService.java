package com.resiflow.service;

import com.resiflow.dto.LogementOccupancyResponse;
import com.resiflow.dto.LogementResponse;
import com.resiflow.dto.ResidenceViewLogementCardResponse;
import com.resiflow.dto.ResidenceViewOverviewResponse;
import com.resiflow.dto.ResidenceViewPaymentStatusResponse;
import com.resiflow.dto.ResidenceViewPendingLogementCardResponse;
import com.resiflow.dto.ResidenceViewResidentResponse;
import com.resiflow.dto.ResidenceViewResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.StatutPaiement;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResidenceViewService {

    private final ResidenceAccessService residenceAccessService;
    private final LogementRepository logementRepository;
    private final UserRepository userRepository;
    private final PaiementRepository paiementRepository;
    private final CagnotteService cagnotteService;
    private final PaymentStatusService paymentStatusService;

    public ResidenceViewService(
            final ResidenceAccessService residenceAccessService,
            final LogementRepository logementRepository,
            final UserRepository userRepository,
            final PaiementRepository paiementRepository,
            final CagnotteService cagnotteService,
            final PaymentStatusService paymentStatusService
    ) {
        this.residenceAccessService = residenceAccessService;
        this.logementRepository = logementRepository;
        this.userRepository = userRepository;
        this.paiementRepository = paiementRepository;
        this.cagnotteService = cagnotteService;
        this.paymentStatusService = paymentStatusService;
    }

    @Transactional(readOnly = true)
    public ResidenceViewResponse getResidenceView(
            final Long residenceId,
            final String search,
            final AuthenticatedUser authenticatedUser
    ) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<Logement> logements = logementRepository.findAllByResidence_IdOrderByNumeroAsc(residenceId);
        List<User> activeResidents = userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                residenceId,
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
        List<User> pendingResidents = userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                residenceId,
                UserStatus.PENDING,
                List.of(UserRole.ADMIN, UserRole.USER)
        );

        Map<Long, List<User>> activeResidentsByLogement = activeResidents.stream()
                .filter(user -> user.getLogementId() != null)
                .collect(java.util.stream.Collectors.groupingBy(User::getLogementId));
        Map<Long, List<User>> pendingResidentsByLogement = pendingResidents.stream()
                .filter(user -> user.getLogementId() != null)
                .collect(java.util.stream.Collectors.groupingBy(User::getLogementId));

        Map<Long, Paiement> latestValidatedByLogement = buildLatestPaiementMap(
                paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                        residenceId,
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                ),
                true
        );
        Map<Long, Paiement> latestPendingByLogement = buildLatestPaiementMap(
                paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementOrderByDatePaiementDesc(
                        residenceId,
                        PaiementStatus.PENDING,
                        TypePaiement.CAGNOTTE
                ),
                false
        );

        User actor = residenceAccessService.getRequiredActor(authenticatedUser);
        Long currentLogementId = actor.getLogementId();
        String normalizedSearch = normalize(search);

        List<ResidenceViewLogementCardResponse> logementCards = logements.stream()
                .filter(logement -> matchesLogementSearch(logement, normalizedSearch))
                .map(logement -> toLogementCard(
                        logement,
                        activeResidentsByLogement.getOrDefault(logement.getId(), List.of()),
                        latestValidatedByLogement.get(logement.getId()),
                        latestPendingByLogement.get(logement.getId())
                ))
                .sorted(logementCardComparator(currentLogementId))
                .toList();

        List<ResidenceViewPendingLogementCardResponse> pendingCards = logements.stream()
                .filter(logement -> matchesLogementSearch(logement, normalizedSearch))
                .filter(logement -> pendingResidentsByLogement.containsKey(logement.getId()))
                .map(logement -> toPendingLogementCard(
                        logement,
                        activeResidentsByLogement.getOrDefault(logement.getId(), List.of()),
                        pendingResidentsByLogement.getOrDefault(logement.getId(), List.of())
                ))
                .sorted(pendingLogementCardComparator(currentLogementId))
                .toList();

        long activeLogements = logements.stream().filter(logement -> Boolean.TRUE.equals(logement.getActive())).count();
        long logementsEnRetard = logements.stream()
                .filter(logement -> Boolean.TRUE.equals(logement.getActive()))
                .filter(logement -> paymentStatusService.calculateStatus(logement) == StatutPaiement.EN_RETARD)
                .count();
        long adminResidents = countByRole(activeResidents, pendingResidents, UserRole.ADMIN);
        long userResidents = countByRole(activeResidents, pendingResidents, UserRole.USER);
        BigDecimal cagnotteSolde = cagnotteService.calculerSolde(residenceId, authenticatedUser);

        ResidenceViewOverviewResponse overview = new ResidenceViewOverviewResponse(
                residenceId,
                (long) logements.size(),
                activeLogements,
                (long) logements.size() - activeLogements,
                (long) activeResidents.size() + pendingResidents.size(),
                (long) activeResidents.size(),
                (long) pendingResidents.size(),
                adminResidents,
                userResidents,
                cagnotteSolde,
                resolveCagnotteStatus(cagnotteSolde),
                activeLogements - logementsEnRetard,
                logementsEnRetard
        );

        return new ResidenceViewResponse(overview, logementCards, pendingCards);
    }

    private ResidenceViewLogementCardResponse toLogementCard(
            final Logement logement,
            final List<User> residents,
            final Paiement lastValidatedPaiement,
            final Paiement pendingPaiement
    ) {
        return new ResidenceViewLogementCardResponse(
                LogementResponse.fromEntity(logement),
                buildOccupancy(logement, residents),
                buildPaymentStatus(logement, lastValidatedPaiement, pendingPaiement),
                residents.stream()
                        .sorted(residentComparator())
                        .map(ResidenceViewResidentResponse::fromUser)
                        .toList()
        );
    }

    private ResidenceViewPendingLogementCardResponse toPendingLogementCard(
            final Logement logement,
            final List<User> existingResidents,
            final List<User> pendingResidents
    ) {
        return new ResidenceViewPendingLogementCardResponse(
                LogementResponse.fromEntity(logement),
                buildOccupancy(logement, existingResidents),
                existingResidents.stream()
                        .sorted(residentComparator())
                        .map(ResidenceViewResidentResponse::fromUser)
                        .toList(),
                pendingResidents.stream()
                        .sorted(residentComparator())
                        .map(ResidenceViewResidentResponse::fromUser)
                        .toList()
        );
    }

    private LogementOccupancyResponse buildOccupancy(final Logement logement, final List<User> activeResidents) {
        int maxOccupants = logement.getResidence().getMaxOccupantsParLogement();
        long occupiedCount = activeResidents.size();
        return new LogementOccupancyResponse(logement.getId(), occupiedCount, maxOccupants, occupiedCount >= maxOccupants);
    }

    private ResidenceViewPaymentStatusResponse buildPaymentStatus(
            final Logement logement,
            final Paiement lastValidatedPaiement,
            final Paiement pendingPaiement
    ) {
        if (!Boolean.TRUE.equals(logement.getActive())) {
            return new ResidenceViewPaymentStatusResponse(
                    "INACTIVE",
                    lastValidatedPaiement == null ? null : lastValidatedPaiement.getDateFin(),
                    false,
                    toPendingPaymentSummary(pendingPaiement)
            );
        }

        LocalDate dateFin = lastValidatedPaiement == null ? null : lastValidatedPaiement.getDateFin();
        LocalDate today = LocalDate.now();
        boolean nextDueWarning = dateFin != null && !dateFin.isBefore(today) && !dateFin.isAfter(today.plusDays(15));

        return new ResidenceViewPaymentStatusResponse(
                paymentStatusService.calculateStatus(logement).name(),
                dateFin,
                nextDueWarning,
                toPendingPaymentSummary(pendingPaiement)
        );
    }

    private ResidenceViewPaymentStatusResponse.PendingPaymentSummary toPendingPaymentSummary(final Paiement pendingPaiement) {
        if (pendingPaiement == null) {
            return null;
        }
        return new ResidenceViewPaymentStatusResponse.PendingPaymentSummary(
                pendingPaiement.getId(),
                pendingPaiement.getMontantTotal(),
                pendingPaiement.getNombreMois(),
                pendingPaiement.getDateDebut(),
                pendingPaiement.getDateFin()
        );
    }

    private Map<Long, Paiement> buildLatestPaiementMap(final List<Paiement> paiements, final boolean compareDateFin) {
        Map<Long, Paiement> latestByLogement = new HashMap<>();
        for (Paiement paiement : paiements) {
            Long logementId = paiement.getLogementId();
            Paiement current = latestByLogement.get(logementId);
            if (current == null) {
                latestByLogement.put(logementId, paiement);
                continue;
            }

            if (compareDateFin) {
                if (isBetterValidatedPaiementCandidate(paiement, current)) {
                    latestByLogement.put(logementId, paiement);
                }
                continue;
            }

            if (paiement.getDatePaiement().isAfter(current.getDatePaiement())) {
                latestByLogement.put(logementId, paiement);
            }
        }
        return latestByLogement;
    }

    private boolean isBetterValidatedPaiementCandidate(final Paiement candidate, final Paiement current) {
        if (current.getDateFin() == null) {
            return candidate.getDateFin() != null;
        }
        if (candidate.getDateFin() == null) {
            return false;
        }
        int dateFinComparison = candidate.getDateFin().compareTo(current.getDateFin());
        if (dateFinComparison != 0) {
            return dateFinComparison > 0;
        }
        return candidate.getDatePaiement() != null
                && (current.getDatePaiement() == null || candidate.getDatePaiement().isAfter(current.getDatePaiement()));
    }

    private Comparator<ResidenceViewLogementCardResponse> logementCardComparator(final Long currentLogementId) {
        return Comparator
                .comparingInt((ResidenceViewLogementCardResponse card) -> logementRank(card, currentLogementId))
                .thenComparing(card -> normalize(card.getLogement().getImmeuble()))
                .thenComparing(card -> normalize(card.getLogement().getNumero()))
                .thenComparing(card -> normalize(card.getLogement().getCodeInterne()));
    }

    private Comparator<ResidenceViewPendingLogementCardResponse> pendingLogementCardComparator(final Long currentLogementId) {
        return Comparator
                .comparingInt((ResidenceViewPendingLogementCardResponse card) -> pendingLogementRank(card, currentLogementId))
                .thenComparing(card -> normalize(card.getLogement().getImmeuble()))
                .thenComparing(card -> normalize(card.getLogement().getNumero()))
                .thenComparing(card -> normalize(card.getLogement().getCodeInterne()));
    }

    private int logementRank(final ResidenceViewLogementCardResponse card, final Long currentLogementId) {
        if (Objects.equals(card.getLogement().getId(), currentLogementId)) {
            return 0;
        }
        boolean hasAdminResident = card.getResidents().stream().anyMatch(resident -> resident.getRole() == UserRole.ADMIN);
        if (hasAdminResident) {
            return 1;
        }
        if ("EN_RETARD".equals(card.getPayment().getStatus())) {
            return 2;
        }
        return 3;
    }

    private int pendingLogementRank(final ResidenceViewPendingLogementCardResponse card, final Long currentLogementId) {
        if (Objects.equals(card.getLogement().getId(), currentLogementId)) {
            return 0;
        }
        boolean hasAdminResident = card.getExistingResidents().stream().anyMatch(resident -> resident.getRole() == UserRole.ADMIN);
        if (hasAdminResident) {
            return 1;
        }
        return 2;
    }

    private Comparator<User> residentComparator() {
        return Comparator
                .comparingInt((User user) -> user.getRole() == UserRole.ADMIN ? 0 : 1)
                .thenComparing(user -> normalize(user.getLastName()))
                .thenComparing(user -> normalize(user.getFirstName()))
                .thenComparing(user -> normalize(user.getEmail()));
    }

    private long countByRole(final List<User> activeResidents, final List<User> pendingResidents, final UserRole role) {
        return java.util.stream.Stream.concat(activeResidents.stream(), pendingResidents.stream())
                .filter(user -> user.getRole() == role)
                .count();
    }

    private boolean matchesLogementSearch(final Logement logement, final String normalizedSearch) {
        if (normalizedSearch == null) {
            return true;
        }
        return contains(logement.getNumero(), normalizedSearch)
                || contains(logement.getImmeuble(), normalizedSearch)
                || contains(logement.getCodeInterne(), normalizedSearch)
                || contains(logement.getAdresse(), normalizedSearch)
                || contains(logement.getEtage(), normalizedSearch);
    }

    private boolean contains(final String value, final String normalizedSearch) {
        return normalize(value) != null && normalize(value).contains(normalizedSearch);
    }

    private String resolveCagnotteStatus(final BigDecimal solde) {
        if (solde == null || solde.signum() == 0) {
            return "NEUTRAL";
        }
        return solde.signum() < 0 ? "NEGATIVE" : "POSITIVE";
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
