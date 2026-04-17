package com.resiflow.service;

import com.resiflow.dto.DepenseResponse;
import com.resiflow.dto.CreateVoteRequest;
import com.resiflow.dto.VoteActionRequest;
import com.resiflow.dto.VoteDetailsResponse;
import com.resiflow.dto.VoteHousingParticipationResponse;
import com.resiflow.dto.VoteOverviewResponse;
import com.resiflow.dto.VoteResultResponse;
import com.resiflow.dto.VoteUtilisateurDetailResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteChoix;
import com.resiflow.entity.VoteStatut;
import com.resiflow.entity.VoteUtilisateur;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.repository.VoteRepository;
import com.resiflow.repository.VoteUtilisateurRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteService.class);

    private final VoteRepository voteRepository;
    private final VoteUtilisateurRepository voteUtilisateurRepository;
    private final UserRepository userRepository;
    private final ResidenceAccessService residenceAccessService;
    private final DepenseService depenseService;

    public VoteService(
            final VoteRepository voteRepository,
            final VoteUtilisateurRepository voteUtilisateurRepository,
            final UserRepository userRepository,
            final ResidenceAccessService residenceAccessService,
            final DepenseService depenseService
    ) {
        this.voteRepository = voteRepository;
        this.voteUtilisateurRepository = voteUtilisateurRepository;
        this.userRepository = userRepository;
        this.residenceAccessService = residenceAccessService;
        this.depenseService = depenseService;
    }

    @Transactional
    public Vote createVote(final CreateVoteRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);

        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        User actor = residenceAccessService.getRequiredActor(authenticatedUser);

        Vote vote = new Vote();
        vote.setResidence(residence);
        vote.setTitre(request.getTitre().trim());
        vote.setDescription(request.getDescription().trim());
        vote.setMontantEstime(request.getMontantEstime());
        vote.setStatut(VoteStatut.OUVERT);
        vote.setDateDebut(request.getDateDebut());
        vote.setDateFin(request.getDateFin());
        vote.setCreePar(actor);

        return voteRepository.save(vote);
    }

    @Transactional(readOnly = true)
    public List<Vote> getVotesByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        List<Vote> votes = voteRepository.findAllByResidence_IdOrderByDateDebutDesc(residenceId);
        votes.forEach(this::refreshVoteStatusIfExpiredReadOnly);
        return votes;
    }

    @Transactional
    public Vote getVote(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureMemberAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);
        return vote;
    }

    @Transactional
    public Vote voter(final Long voteId, final VoteActionRequest request, final AuthenticatedUser authenticatedUser) {
        validateVoteRequest(request);

        Vote vote = getRequiredVote(voteId);
        ensureMemberAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);

        if (vote.getStatut() != VoteStatut.OUVERT) {
            throw new IllegalStateException("Vote is not open");
        }

        User actor = residenceAccessService.getRequiredActor(authenticatedUser);
        if (voteUtilisateurRepository.existsByVote_IdAndUtilisateur_Id(vote.getId(), actor.getId())) {
            throw new IllegalStateException("User has already voted for this vote");
        }

        VoteUtilisateur voteUtilisateur = new VoteUtilisateur();
        voteUtilisateur.setVote(vote);
        voteUtilisateur.setUtilisateur(actor);
        voteUtilisateur.setChoix(parseChoix(request.getChoix()));
        voteUtilisateur.setCommentaire(normalizeCommentaire(request.getCommentaire()));
        voteUtilisateur.setDateVote(LocalDateTime.now());
        voteUtilisateurRepository.save(voteUtilisateur);

        return vote;
    }

    @Transactional
    public Vote removeUserVote(final Long voteId, final Long userId, final AuthenticatedUser authenticatedUser) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);

        if (vote.getStatut() != VoteStatut.OUVERT) {
            throw new IllegalStateException("User vote can only be removed from an open vote");
        }
        if (!voteUtilisateurRepository.existsByVote_IdAndUtilisateur_Id(vote.getId(), userId)) {
            throw new NoSuchElementException("User vote not found for vote " + voteId + " and user " + userId);
        }

        voteUtilisateurRepository.deleteByVote_IdAndUtilisateur_Id(vote.getId(), userId);
        LOGGER.info("Removed user {} vote from vote {}", userId, voteId);
        return vote;
    }

    @Transactional
    public Vote closeVote(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);
        if (vote.getStatut() != VoteStatut.OUVERT) {
            throw new IllegalStateException("Only open votes can be closed");
        }
        vote.setDateFin(LocalDateTime.now());
        finalizeVote(vote);
        LOGGER.info("Vote {} closed manually with status {}", vote.getId(), vote.getStatut());
        return voteRepository.save(vote);
    }

    @Transactional
    public Vote reopenVote(final Long voteId, final VoteActionRequest request, final AuthenticatedUser authenticatedUser) {
        if (request == null || request.getNouvelleDateFin() == null) {
            throw new IllegalArgumentException("New end date must not be null");
        }

        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);

        if (vote.getDepense() != null) {
            throw new IllegalStateException("Vote linked to a depense cannot be reopened");
        }
        if (!request.getNouvelleDateFin().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("New end date must be in the future");
        }
        if (!request.getNouvelleDateFin().isAfter(vote.getDateDebut())) {
            throw new IllegalArgumentException("New end date must be after start date");
        }

        vote.setStatut(VoteStatut.OUVERT);
        vote.setDateFin(request.getNouvelleDateFin());
        return voteRepository.save(vote);
    }

    @Transactional
    public void deleteVote(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);

        if (vote.getDepense() != null) {
            throw new IllegalStateException("Vote linked to a depense cannot be deleted");
        }
        if (vote.getStatut() == VoteStatut.VALIDE) {
            throw new IllegalStateException("Validated vote cannot be deleted");
        }

        voteRepository.delete(vote);
    }

    @Transactional
    public VoteResultResponse getVoteResult(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureMemberAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);
        return buildResult(vote);
    }

    @Transactional(readOnly = true)
    public VoteDetailsResponse getVoteDetails(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);

        List<VoteUtilisateurDetailResponse> votesUtilisateurs = voteUtilisateurRepository
                .findAllByVote_IdOrderByDateVoteAsc(voteId)
                .stream()
                .map(VoteUtilisateurDetailResponse::fromEntity)
                .toList();

        return new VoteDetailsResponse(voteId, votesUtilisateurs);
    }

    @Transactional
    public DepenseResponse createDepenseFromVote(
            final Long voteId,
            final VoteActionRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        Vote vote = getRequiredVote(voteId);
        ensureAdminAccess(vote, authenticatedUser);
        refreshVoteStatusIfExpired(vote);

        if (vote.getStatut() != VoteStatut.VALIDE) {
            throw new IllegalStateException("Only validated votes can create a depense");
        }
        if (vote.getDepense() != null) {
            throw new IllegalStateException("A depense is already linked to this vote");
        }

        BigDecimal montant = request == null || request.getMontant() == null ? vote.getMontantEstime() : request.getMontant();
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Depense montant must be greater than zero");
        }

        User actor = residenceAccessService.getRequiredActor(authenticatedUser);
        Depense depense = depenseService.createDepenseFromVote(vote, montant, actor);
        vote.setDepense(depense);
        voteRepository.save(vote);
        return DepenseResponse.fromEntity(depense);
    }

    @Transactional(readOnly = true)
    public List<VoteOverviewResponse> getVoteOverviewsByResidence(
            final Long residenceId,
            final AuthenticatedUser authenticatedUser
    ) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<Vote> votes = voteRepository.findAllByResidence_IdOrderByDateDebutDesc(residenceId);
        if (votes.isEmpty()) {
            return List.of();
        }

        List<User> eligibleUsers = getEligibleUsersForResidence(residenceId);
        Map<Long, List<VoteUtilisateur>> voteRecordsByVoteId = groupVoteRecordsByVoteId(votes);

        return votes.stream()
                .map(vote -> buildOverview(vote, eligibleUsers, voteRecordsByVoteId.getOrDefault(vote.getId(), List.of()), authenticatedUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public VoteOverviewResponse getVoteOverview(final Long voteId, final AuthenticatedUser authenticatedUser) {
        Vote vote = getRequiredVote(voteId);
        ensureMemberAccess(vote, authenticatedUser);

        List<User> eligibleUsers = getEligibleUsersForResidence(vote.getResidence().getId());
        List<VoteUtilisateur> voteRecords = voteUtilisateurRepository.findAllByVote_IdOrderByDateVoteAsc(voteId);
        return buildOverview(vote, eligibleUsers, voteRecords, authenticatedUser);
    }

    @Transactional
    public int closeExpiredVotes() {
        List<Vote> expiredVotes = voteRepository.findAllByStatutAndDateFinBefore(VoteStatut.OUVERT, LocalDateTime.now());
        int closedVotes = 0;
        for (Vote vote : expiredVotes) {
            finalizeVote(vote);
            closedVotes++;
            LOGGER.info("Vote {} closed automatically with status {}", vote.getId(), vote.getStatut());
        }
        return closedVotes;
    }

    private Vote getRequiredVote(final Long voteId) {
        if (voteId == null) {
            throw new IllegalArgumentException("Vote ID must not be null");
        }
        return voteRepository.findById(voteId)
                .orElseThrow(() -> new NoSuchElementException("Vote not found: " + voteId));
    }

    private void validateCreateRequest(final CreateVoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create vote request must not be null");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        if (isBlank(request.getTitre())) {
            throw new IllegalArgumentException("Vote title must not be blank");
        }
        if (isBlank(request.getDescription())) {
            throw new IllegalArgumentException("Vote description must not be blank");
        }
        if (request.getMontantEstime() != null && request.getMontantEstime().signum() <= 0) {
            throw new IllegalArgumentException("Estimated amount must be greater than zero");
        }
        if (request.getDateDebut() == null) {
            throw new IllegalArgumentException("Vote start date must not be null");
        }
        if (request.getDateFin() == null) {
            throw new IllegalArgumentException("Vote end date must not be null");
        }
        if (!request.getDateFin().isAfter(request.getDateDebut())) {
            throw new IllegalArgumentException("Vote end date must be after start date");
        }
    }

    private void validateVoteRequest(final VoteActionRequest request) {
        if (request == null || isBlank(request.getChoix())) {
            throw new IllegalArgumentException("Vote choice must not be blank");
        }
    }

    private VoteChoix parseChoix(final String rawChoix) {
        try {
            return VoteChoix.valueOf(rawChoix.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Vote choice must be POUR, CONTRE or NEUTRE");
        }
    }

    private void ensureMemberAccess(final Vote vote, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.ensureMemberAccessToResidence(authenticatedUser, vote.getResidence().getId());
    }

    private void ensureAdminAccess(final Vote vote, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, vote.getResidence().getId());
    }

    private void refreshVoteStatusIfExpired(final Vote vote) {
        if (vote.getStatut() == VoteStatut.OUVERT && !vote.getDateFin().isAfter(LocalDateTime.now())) {
            finalizeVote(vote);
            voteRepository.save(vote);
        }
    }

    private void refreshVoteStatusIfExpiredReadOnly(final Vote vote) {
        if (vote.getStatut() == VoteStatut.OUVERT && !vote.getDateFin().isAfter(LocalDateTime.now())) {
            LOGGER.debug("Vote {} is expired and will be finalized by scheduler or explicit refresh", vote.getId());
        }
    }

    private void finalizeVote(final Vote vote) {
        long totalPour = voteUtilisateurRepository.countByVote_IdAndChoix(vote.getId(), VoteChoix.POUR);
        long totalContre = voteUtilisateurRepository.countByVote_IdAndChoix(vote.getId(), VoteChoix.CONTRE);
        vote.setStatut(totalPour > totalContre ? VoteStatut.VALIDE : VoteStatut.REJETE);
    }

    private VoteResultResponse buildResult(final Vote vote) {
        long totalPour = voteUtilisateurRepository.countByVote_IdAndChoix(vote.getId(), VoteChoix.POUR);
        long totalContre = voteUtilisateurRepository.countByVote_IdAndChoix(vote.getId(), VoteChoix.CONTRE);
        long totalNeutre = voteUtilisateurRepository.countByVote_IdAndChoix(vote.getId(), VoteChoix.NEUTRE);
        return new VoteResultResponse(vote.getId(), totalPour, totalContre, totalNeutre, vote.getStatut());
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeCommentaire(final String commentaire) {
        return isBlank(commentaire) ? null : commentaire.trim();
    }

    private VoteOverviewResponse buildOverview(
            final Vote vote,
            final List<User> eligibleUsers,
            final List<VoteUtilisateur> voteRecords,
            final AuthenticatedUser authenticatedUser
    ) {
        long totalPour = countChoice(voteRecords, VoteChoix.POUR);
        long totalContre = countChoice(voteRecords, VoteChoix.CONTRE);
        long totalNeutre = countChoice(voteRecords, VoteChoix.NEUTRE);
        long totalVotants = voteRecords.size();
        VoteStatut effectiveStatut = resolveEffectiveStatut(vote, totalPour, totalContre);
        boolean isOpen = isVoteOpen(vote);

        Map<Long, List<User>> eligibleUsersByLogement = new HashMap<>();
        for (User eligibleUser : eligibleUsers) {
            if (eligibleUser.getLogementId() == null) {
                continue;
            }
            eligibleUsersByLogement.computeIfAbsent(eligibleUser.getLogementId(), ignored -> new java.util.ArrayList<>())
                    .add(eligibleUser);
        }

        Map<Long, Long> votedByLogement = new HashMap<>();
        Map<Long, VoteUtilisateur> userVoteByUserId = new HashMap<>();
        for (VoteUtilisateur voteRecord : voteRecords) {
            Long userId = voteRecord.getUtilisateur().getId();
            userVoteByUserId.put(userId, voteRecord);
            Long logementId = voteRecord.getUtilisateur().getLogementId();
            if (logementId != null) {
                votedByLogement.merge(logementId, 1L, Long::sum);
            }
        }

        VoteUtilisateur currentUserVote = authenticatedUser == null || authenticatedUser.userId() == null
                ? null
                : userVoteByUserId.get(authenticatedUser.userId());
        boolean currentUserHasVoted = currentUserVote != null;
        String currentUserChoice = currentUserVote == null ? null : currentUserVote.getChoix().name();
        String currentUserComment = currentUserVote == null ? null : currentUserVote.getCommentaire();

        List<VoteHousingParticipationResponse> participationsLogements = eligibleUsersByLogement.entrySet().stream()
                .map(entry -> {
                    List<User> logementUsers = entry.getValue();
                    Logement logement = logementUsers.get(0).getLogement();
                    long totalEligibleVoters = logementUsers.size();
                    long logementVoters = votedByLogement.getOrDefault(entry.getKey(), 0L);
                    return new VoteHousingParticipationResponse(
                            entry.getKey(),
                            logement == null ? null : logement.getCodeInterne(),
                            totalEligibleVoters,
                            logementVoters,
                            logementVoters > 0
                    );
                })
                .sorted(Comparator.comparing(
                        participation -> normalizeForSort(participation.getCodeInterne()),
                        Comparator.nullsLast(String::compareTo)
                ))
                .toList();

        long joursRestants = calculateDaysRemaining(vote.getDateFin());
        boolean finProche = isOpen && joursRestants > 0 && joursRestants <= 3;
        boolean currentUserEligible = authenticatedUser != null
                && authenticatedUser.userId() != null
                && eligibleUsers.stream().anyMatch(user -> Objects.equals(user.getId(), authenticatedUser.userId()));

        return new VoteOverviewResponse(
                vote.getId(),
                vote.getResidence().getId(),
                vote.getTitre(),
                vote.getDescription(),
                vote.getMontantEstime(),
                effectiveStatut,
                isOpen ? "EN_COURS" : "TERMINE",
                vote.getDateDebut(),
                vote.getDateFin(),
                vote.getCreePar().getId(),
                buildUserDisplayName(vote.getCreePar()),
                vote.getDepense() == null ? null : vote.getDepense().getId(),
                totalPour,
                totalContre,
                totalNeutre,
                totalVotants,
                eligibleUsers.size(),
                resolveLeadingChoice(totalPour, totalContre, totalNeutre),
                currentUserHasVoted,
                currentUserChoice,
                currentUserComment,
                isOpen && currentUserEligible && !currentUserHasVoted,
                joursRestants,
                finProche,
                participationsLogements
        );
    }

    private List<User> getEligibleUsersForResidence(final Long residenceId) {
        return userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                residenceId,
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
    }

    private Map<Long, List<VoteUtilisateur>> groupVoteRecordsByVoteId(final List<Vote> votes) {
        List<Long> voteIds = votes.stream().map(Vote::getId).toList();
        List<VoteUtilisateur> allVoteRecords = voteUtilisateurRepository.findAllByVote_IdIn(voteIds);
        Map<Long, List<VoteUtilisateur>> recordsByVoteId = new HashMap<>();
        for (VoteUtilisateur voteRecord : allVoteRecords) {
            recordsByVoteId.computeIfAbsent(voteRecord.getVote().getId(), ignored -> new java.util.ArrayList<>())
                    .add(voteRecord);
        }
        return recordsByVoteId;
    }

    private long countChoice(final List<VoteUtilisateur> voteRecords, final VoteChoix choix) {
        return voteRecords.stream().filter(voteRecord -> voteRecord.getChoix() == choix).count();
    }

    private VoteStatut resolveEffectiveStatut(final Vote vote, final long totalPour, final long totalContre) {
        if (vote.getStatut() != VoteStatut.OUVERT) {
            return vote.getStatut();
        }
        if (vote.getDateFin().isAfter(LocalDateTime.now())) {
            return vote.getStatut();
        }
        return totalPour > totalContre ? VoteStatut.VALIDE : VoteStatut.REJETE;
    }

    private boolean isVoteOpen(final Vote vote) {
        return vote.getStatut() == VoteStatut.OUVERT && vote.getDateFin().isAfter(LocalDateTime.now());
    }

    private String resolveLeadingChoice(final long totalPour, final long totalContre, final long totalNeutre) {
        long max = Math.max(totalPour, Math.max(totalContre, totalNeutre));
        if (max == 0) {
            return "AUCUN";
        }

        int winners = 0;
        String leadingChoice = null;
        if (totalPour == max) {
            winners++;
            leadingChoice = VoteChoix.POUR.name();
        }
        if (totalContre == max) {
            winners++;
            leadingChoice = VoteChoix.CONTRE.name();
        }
        if (totalNeutre == max) {
            winners++;
            leadingChoice = VoteChoix.NEUTRE.name();
        }
        return winners > 1 ? "EGALITE" : leadingChoice;
    }

    private long calculateDaysRemaining(final LocalDateTime endDate) {
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
        return Math.max(days, 0);
    }

    private String buildUserDisplayName(final User user) {
        if (user == null) {
            return "";
        }
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }
        return user.getEmail() == null ? "" : user.getEmail().trim();
    }

    private String normalizeForSort(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
