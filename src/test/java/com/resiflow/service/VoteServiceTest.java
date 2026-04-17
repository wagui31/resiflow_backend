package com.resiflow.service;

import com.resiflow.dto.VoteActionRequest;
import com.resiflow.dto.VoteDetailsResponse;
import com.resiflow.dto.VoteOverviewResponse;
import com.resiflow.dto.VoteResultResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteChoix;
import com.resiflow.entity.VoteStatut;
import com.resiflow.entity.VoteUtilisateur;
import com.resiflow.repository.UserRepository;
import com.resiflow.repository.VoteRepository;
import com.resiflow.repository.VoteUtilisateurRepository;
import com.resiflow.security.AuthenticatedUser;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoteServiceTest {

    @Test
    void voterAcceptsNeutralChoice() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.OUVERT);
        User actor = buildUser(21L, 7L);
        AtomicReference<VoteUtilisateur> savedVoteRef = new AtomicReference<>();
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(savedVoteRef, Map.of(VoteChoix.POUR, 0L, VoteChoix.CONTRE, 0L, VoteChoix.NEUTRE, 0L), false),
                userRepositoryProxy(List.of(actor)),
                residenceAccessServiceStub(actor),
                depenseServiceStub()
        );

        VoteActionRequest request = new VoteActionRequest();
        request.setChoix(" neutre ");
        request.setCommentaire("  Avis neutre  ");

        voteService.voter(10L, request, new AuthenticatedUser(21L, "user@example.com", 7L, UserRole.USER));

        assertThat(savedVoteRef.get()).isNotNull();
        assertThat(savedVoteRef.get().getChoix()).isEqualTo(VoteChoix.NEUTRE);
        assertThat(savedVoteRef.get().getCommentaire()).isEqualTo("Avis neutre");
    }

    @Test
    void voterRejectsBlankChoice() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.OUVERT);
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(new AtomicReference<>(), Map.of(VoteChoix.POUR, 0L, VoteChoix.CONTRE, 0L, VoteChoix.NEUTRE, 0L), false),
                userRepositoryProxy(List.of(buildUser(21L, 7L))),
                residenceAccessServiceStub(buildUser(21L, 7L)),
                depenseServiceStub()
        );

        VoteActionRequest request = new VoteActionRequest();
        request.setChoix("   ");

        assertThatThrownBy(() -> voteService.voter(10L, request, new AuthenticatedUser(21L, "user@example.com", 7L, UserRole.USER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vote choice must not be blank");
    }

    @Test
    void getVoteResultReturnsNeutralCountAndNeutralDoesNotAffectDecision() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().minusMinutes(1), VoteStatut.OUVERT);
        AtomicReference<Vote> savedVoteRef = new AtomicReference<>();
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, savedVoteRef),
                voteUtilisateurRepositoryProxy(
                        new AtomicReference<>(),
                        Map.of(VoteChoix.POUR, 2L, VoteChoix.CONTRE, 1L, VoteChoix.NEUTRE, 5L),
                        false
                ),
                userRepositoryProxy(List.of(buildUser(21L, 7L))),
                residenceAccessServiceStub(buildUser(21L, 7L)),
                depenseServiceStub()
        );

        VoteResultResponse result = voteService.getVoteResult(10L, new AuthenticatedUser(21L, "user@example.com", 7L, UserRole.USER));

        assertThat(result.getTotalPour()).isEqualTo(2L);
        assertThat(result.getTotalContre()).isEqualTo(1L);
        assertThat(result.getTotalNeutre()).isEqualTo(5L);
        assertThat(result.getStatut()).isEqualTo(VoteStatut.VALIDE);
        assertThat(savedVoteRef.get()).isNotNull();
        assertThat(savedVoteRef.get().getStatut()).isEqualTo(VoteStatut.VALIDE);
    }

    @Test
    void getVoteDetailsReturnsChoiceAndCommentaire() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.OUVERT);
        VoteUtilisateur voteUtilisateur = new VoteUtilisateur();
        voteUtilisateur.setVote(vote);
        voteUtilisateur.setUtilisateur(buildUserWithEmail(21L, 7L, "user@example.com"));
        voteUtilisateur.setChoix(VoteChoix.POUR);
        voteUtilisateur.setCommentaire("Bon pour moi");
        voteUtilisateur.setDateVote(LocalDateTime.now().minusHours(1));

        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(new AtomicReference<>(), Map.of(), false, new AtomicBoolean(false), List.of(voteUtilisateur)),
                userRepositoryProxy(List.of(buildUser(21L, 7L))),
                residenceAccessServiceStub(buildUser(21L, 7L)),
                depenseServiceStub()
        );

        VoteDetailsResponse response = voteService.getVoteDetails(
                10L,
                new AuthenticatedUser(21L, "user@example.com", 7L, UserRole.USER)
        );

        assertThat(response.getVoteId()).isEqualTo(10L);
        assertThat(response.getVotesUtilisateurs()).hasSize(1);
        assertThat(response.getVotesUtilisateurs().get(0).getUserId()).isEqualTo(21L);
        assertThat(response.getVotesUtilisateurs().get(0).getUserEmail()).isEqualTo("user@example.com");
        assertThat(response.getVotesUtilisateurs().get(0).getLogementId()).isEqualTo(100L);
        assertThat(response.getVotesUtilisateurs().get(0).getLogementCodeInterne()).isEqualTo("LOG-A");
        assertThat(response.getVotesUtilisateurs().get(0).getChoix()).isEqualTo("POUR");
        assertThat(response.getVotesUtilisateurs().get(0).getCommentaire()).isEqualTo("Bon pour moi");
    }

    @Test
    void removeUserVoteDeletesVoteForOpenVote() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.OUVERT);
        AtomicBoolean deleted = new AtomicBoolean(false);
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(new AtomicReference<>(), Map.of(), true, deleted),
                userRepositoryProxy(List.of(buildUser(99L, 7L))),
                residenceAccessServiceStub(buildUser(99L, 7L)),
                depenseServiceStub()
        );

        Vote returnedVote = voteService.removeUserVote(10L, 21L, new AuthenticatedUser(99L, "admin@example.com", 7L, UserRole.ADMIN));

        assertThat(returnedVote).isSameAs(vote);
        assertThat(deleted).isTrue();
    }

    @Test
    void removeUserVoteRejectsClosedVote() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.VALIDE);
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(new AtomicReference<>(), Map.of(), true, new AtomicBoolean(false)),
                userRepositoryProxy(List.of(buildUser(99L, 7L))),
                residenceAccessServiceStub(buildUser(99L, 7L)),
                depenseServiceStub()
        );

        assertThatThrownBy(() -> voteService.removeUserVote(
                10L,
                21L,
                new AuthenticatedUser(99L, "admin@example.com", 7L, UserRole.ADMIN)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User vote can only be removed from an open vote");
    }

    @Test
    void removeUserVoteRejectsMissingVoteEntry() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(1), VoteStatut.OUVERT);
        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(new AtomicReference<>(), Map.of(), false, new AtomicBoolean(false)),
                userRepositoryProxy(List.of(buildUser(99L, 7L))),
                residenceAccessServiceStub(buildUser(99L, 7L)),
                depenseServiceStub()
        );

        assertThatThrownBy(() -> voteService.removeUserVote(
                10L,
                21L,
                new AuthenticatedUser(99L, "admin@example.com", 7L, UserRole.ADMIN)
        ))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessage("User vote not found for vote 10 and user 21");
    }

    @Test
    void getVoteOverviewAggregatesParticipationByHousingWithoutExposingChoicesPerHousing() {
        Vote vote = buildVote(10L, 7L, LocalDateTime.now().plusDays(2), VoteStatut.OUVERT);
        User currentUser = buildUserWithHousing(21L, 7L, 100L, "LOG-A", "current@example.com", "Current", "User");
        User secondHousingUser = buildUserWithHousing(22L, 7L, 100L, "LOG-A", "second@example.com", "Second", "User");
        User thirdHousingUser = buildUserWithHousing(23L, 7L, 101L, "LOG-B", "third@example.com", "Third", "User");

        VoteUtilisateur currentUserVote = buildVoteUtilisateur(vote, currentUser, VoteChoix.POUR);
        VoteUtilisateur thirdUserVote = buildVoteUtilisateur(vote, thirdHousingUser, VoteChoix.CONTRE);

        VoteService voteService = new VoteService(
                voteRepositoryProxy(vote, new AtomicReference<>()),
                voteUtilisateurRepositoryProxy(
                        new AtomicReference<>(),
                        Map.of(VoteChoix.POUR, 1L, VoteChoix.CONTRE, 1L, VoteChoix.NEUTRE, 0L),
                        false,
                        new AtomicBoolean(false),
                        List.of(currentUserVote, thirdUserVote)
                ),
                userRepositoryProxy(List.of(currentUser, secondHousingUser, thirdHousingUser)),
                residenceAccessServiceStub(currentUser),
                depenseServiceStub()
        );

        VoteOverviewResponse response = voteService.getVoteOverview(
                10L,
                new AuthenticatedUser(21L, "current@example.com", 7L, UserRole.USER)
        );

        assertThat(response.getTotalVotantsEligibles()).isEqualTo(3L);
        assertThat(response.getTotalVotants()).isEqualTo(2L);
        assertThat(response.getTotalPour()).isEqualTo(1L);
        assertThat(response.getTotalContre()).isEqualTo(1L);
        assertThat(response.getChoixMajoritaire()).isEqualTo("EGALITE");
        assertThat(response.isCurrentUserHasVoted()).isTrue();
        assertThat(response.getCurrentUserChoice()).isEqualTo("POUR");
        assertThat(response.getCurrentUserComment()).isEqualTo("Je valide");
        assertThat(response.isCurrentUserCanVote()).isFalse();
        assertThat(response.getParticipationsLogements()).hasSize(2);
        assertThat(response.getParticipationsLogements().get(0).getCodeInterne()).isEqualTo("LOG-A");
        assertThat(response.getParticipationsLogements().get(0).getTotalVoters()).isEqualTo(1L);
        assertThat(response.getParticipationsLogements().get(0).getTotalEligibleVoters()).isEqualTo(2L);
        assertThat(response.getParticipationsLogements().get(1).getCodeInterne()).isEqualTo("LOG-B");
        assertThat(response.getParticipationsLogements().get(1).getTotalVoters()).isEqualTo(1L);
    }

    private VoteRepository voteRepositoryProxy(final Vote voteToReturn, final AtomicReference<Vote> savedVoteRef) {
        return (VoteRepository) Proxy.newProxyInstance(
                VoteRepository.class.getClassLoader(),
                new Class<?>[]{VoteRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(voteToReturn);
                    }
                    if ("save".equals(method.getName())) {
                        Vote vote = (Vote) args[0];
                        savedVoteRef.set(vote);
                        return vote;
                    }
                    if ("findAllByStatutAndDateFinBefore".equals(method.getName())) {
                        return List.of();
                    }
                    if ("findAllByResidence_IdOrderByDateDebutDesc".equals(method.getName())) {
                        return List.of(voteToReturn);
                    }
                    if ("toString".equals(method.getName())) {
                        return "VoteRepositoryTestProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                });
    }

    private VoteUtilisateurRepository voteUtilisateurRepositoryProxy(
            final AtomicReference<VoteUtilisateur> savedVoteRef,
            final Map<VoteChoix, Long> counts,
            final boolean alreadyVoted
    ) {
        return voteUtilisateurRepositoryProxy(savedVoteRef, counts, alreadyVoted, new AtomicBoolean(false), List.of());
    }

    private VoteUtilisateurRepository voteUtilisateurRepositoryProxy(
            final AtomicReference<VoteUtilisateur> savedVoteRef,
            final Map<VoteChoix, Long> counts,
            final boolean alreadyVoted,
            final AtomicBoolean deleted
    ) {
        return voteUtilisateurRepositoryProxy(savedVoteRef, counts, alreadyVoted, deleted, List.of());
    }

    private VoteUtilisateurRepository voteUtilisateurRepositoryProxy(
            final AtomicReference<VoteUtilisateur> savedVoteRef,
            final Map<VoteChoix, Long> counts,
            final boolean alreadyVoted,
            final AtomicBoolean deleted,
            final List<VoteUtilisateur> voteUtilisateurs
    ) {
        return (VoteUtilisateurRepository) Proxy.newProxyInstance(
                VoteUtilisateurRepository.class.getClassLoader(),
                new Class<?>[]{VoteUtilisateurRepository.class},
                (proxy, method, args) -> {
                    if ("existsByVote_IdAndUtilisateur_Id".equals(method.getName())) {
                        return alreadyVoted;
                    }
                    if ("countByVote_IdAndChoix".equals(method.getName())) {
                        VoteChoix choix = (VoteChoix) args[1];
                        return counts.getOrDefault(choix, 0L);
                    }
                    if ("save".equals(method.getName())) {
                        VoteUtilisateur voteUtilisateur = (VoteUtilisateur) args[0];
                        savedVoteRef.set(voteUtilisateur);
                        return voteUtilisateur;
                    }
                    if ("deleteByVote_IdAndUtilisateur_Id".equals(method.getName())) {
                        deleted.set(true);
                        return null;
                    }
                    if ("findAllByVote_IdOrderByDateVoteAsc".equals(method.getName())) {
                        return voteUtilisateurs;
                    }
                    if ("findAllByVote_IdIn".equals(method.getName())) {
                        return voteUtilisateurs;
                    }
                    if ("toString".equals(method.getName())) {
                        return "VoteUtilisateurRepositoryTestProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                });
    }

    private UserRepository userRepositoryProxy(final List<User> eligibleUsers) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findAllByResidence_IdAndStatusAndRoleIn".equals(method.getName())) {
                        return eligibleUsers;
                    }
                    if ("findById".equals(method.getName())) {
                        Long userId = (Long) args[0];
                        return eligibleUsers.stream()
                                .filter(user -> userId.equals(user.getId()))
                                .findFirst();
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                });
    }

    private ResidenceAccessService residenceAccessServiceStub(final User actor) {
        return new ResidenceAccessService(null, null) {
            @Override
            public User getRequiredActor(final AuthenticatedUser authenticatedUser) {
                return actor;
            }

            @Override
            public void ensureMemberAccessToResidence(final AuthenticatedUser authenticatedUser, final Long residenceId) {
                // No-op for service unit tests.
            }

            @Override
            public void ensureAdminAccessToResidence(final AuthenticatedUser authenticatedUser, final Long residenceId) {
                // No-op for service unit tests.
            }
        };
    }

    private DepenseService depenseServiceStub() {
        return new DepenseService(null, null, null, null) {
        };
    }

    private Vote buildVote(final Long voteId, final Long residenceId, final LocalDateTime dateFin, final VoteStatut statut) {
        Residence residence = new Residence();
        residence.setId(residenceId);

        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setResidence(residence);
        vote.setTitre("Vote travaux");
        vote.setDescription("Description");
        vote.setStatut(statut);
        vote.setDateDebut(LocalDateTime.now().minusDays(1));
        vote.setDateFin(dateFin);
        vote.setCreePar(buildUser(99L, residenceId));
        return vote;
    }

    private User buildUser(final Long userId, final Long residenceId) {
        return buildUserWithEmail(userId, residenceId, "user@example.com");
    }

    private User buildUserWithEmail(final Long userId, final Long residenceId, final String email) {
        return buildUserWithHousing(userId, residenceId, 100L, "LOG-A", email, "Test", "User");
    }

    private User buildUserWithHousing(
            final Long userId,
            final Long residenceId,
            final Long logementId,
            final String codeInterne,
            final String email,
            final String firstName,
            final String lastName
    ) {
        Residence residence = new Residence();
        residence.setId(residenceId);

        Logement logement = new Logement();
        logement.setId(logementId);
        logement.setCodeInterne(codeInterne);
        logement.setResidence(residence);

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setResidence(residence);
        user.setLogement(logement);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private VoteUtilisateur buildVoteUtilisateur(final Vote vote, final User user, final VoteChoix choix) {
        VoteUtilisateur voteUtilisateur = new VoteUtilisateur();
        voteUtilisateur.setVote(vote);
        voteUtilisateur.setUtilisateur(user);
        voteUtilisateur.setChoix(choix);
        voteUtilisateur.setCommentaire("Je valide");
        voteUtilisateur.setDateVote(LocalDateTime.now().minusHours(1));
        return voteUtilisateur;
    }
}
