package com.resiflow.service;

import com.resiflow.dto.DepenseResponse;
import com.resiflow.dto.CreateVoteRequest;
import com.resiflow.dto.VoteActionRequest;
import com.resiflow.dto.VoteDetailsResponse;
import com.resiflow.dto.VoteResultResponse;
import com.resiflow.dto.VoteUtilisateurDetailResponse;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteChoix;
import com.resiflow.entity.VoteStatut;
import com.resiflow.entity.VoteUtilisateur;
import com.resiflow.repository.VoteRepository;
import com.resiflow.repository.VoteUtilisateurRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteService.class);

    private final VoteRepository voteRepository;
    private final VoteUtilisateurRepository voteUtilisateurRepository;
    private final ResidenceAccessService residenceAccessService;
    private final DepenseService depenseService;

    public VoteService(
            final VoteRepository voteRepository,
            final VoteUtilisateurRepository voteUtilisateurRepository,
            final ResidenceAccessService residenceAccessService,
            final DepenseService depenseService
    ) {
        this.voteRepository = voteRepository;
        this.voteUtilisateurRepository = voteUtilisateurRepository;
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
        ensureMemberAccess(vote, authenticatedUser);

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
}
