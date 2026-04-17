package com.resiflow.controller;

import com.resiflow.dto.VoteHousingParticipationResponse;
import com.resiflow.dto.VoteOverviewResponse;
import com.resiflow.dto.VoteResultResponse;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.Vote;
import com.resiflow.entity.VoteStatut;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.VoteService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VoteControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        VoteService voteService = new VoteService(null, null, null, null, null) {
            @Override
            public List<VoteOverviewResponse> getVoteOverviewsByResidence(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(buildOverview(10L, residenceId));
            }

            @Override
            public VoteOverviewResponse getVoteOverview(final Long voteId, final AuthenticatedUser authenticatedUser) {
                return buildOverview(voteId, authenticatedUser.residenceId());
            }

            @Override
            public VoteResultResponse getVoteResult(final Long voteId, final AuthenticatedUser authenticatedUser) {
                return new VoteResultResponse(voteId, 5L, 2L, 1L, VoteStatut.VALIDE);
            }

            @Override
            public Vote getVote(final Long voteId, final AuthenticatedUser authenticatedUser) {
                Residence residence = new Residence();
                residence.setId(authenticatedUser.residenceId());

                User creator = new User();
                creator.setId(2L);

                Vote vote = new Vote();
                vote.setId(voteId);
                vote.setResidence(residence);
                vote.setTitre("Travaux ascenseur");
                vote.setDescription("Vote travaux ascenseur");
                vote.setMontantEstime(new BigDecimal("1200.00"));
                vote.setStatut(VoteStatut.OUVERT);
                vote.setDateDebut(LocalDateTime.of(2026, 4, 1, 10, 0));
                vote.setDateFin(LocalDateTime.of(2026, 4, 20, 18, 0));
                vote.setCreePar(creator);
                return vote;
            }

            private VoteOverviewResponse buildOverview(final Long voteId, final Long residenceId) {
                return new VoteOverviewResponse(
                        voteId,
                        residenceId,
                        "Travaux ascenseur",
                        "Vote travaux ascenseur",
                        new BigDecimal("1200.00"),
                        VoteStatut.OUVERT,
                        "EN_COURS",
                        LocalDateTime.of(2026, 4, 1, 10, 0),
                        LocalDateTime.of(2026, 4, 20, 18, 0),
                        2L,
                        "Admin Residence",
                        null,
                        5L,
                        2L,
                        1L,
                        8L,
                        12L,
                        "POUR",
                        true,
                        "POUR",
                        "Je valide",
                        false,
                        2L,
                        true,
                        List.of(
                                new VoteHousingParticipationResponse(100L, "LOG-A", 3L, 2L, true),
                                new VoteHousingParticipationResponse(101L, "LOG-B", 2L, 0L, false)
                        )
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new VoteController(voteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getVoteOverviewsByResidenceReturnsAggregatedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/votes/residence/7/overview")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].statutAffichage").value("EN_COURS"))
                .andExpect(jsonPath("$[0].totalVotants").value(8))
                .andExpect(jsonPath("$[0].totalVotantsEligibles").value(12))
                .andExpect(jsonPath("$[0].choixMajoritaire").value("POUR"))
                .andExpect(jsonPath("$[0].currentUserHasVoted").value(true))
                .andExpect(jsonPath("$[0].currentUserComment").value("Je valide"))
                .andExpect(jsonPath("$[0].participationsLogements[0].codeInterne").value("LOG-A"))
                .andExpect(jsonPath("$[0].participationsLogements[0].totalVoters").value(2))
                .andExpect(jsonPath("$[0].participationsLogements[1].hasVoted").value(false));
    }

    @Test
    void getVoteOverviewReturnsSingleAggregatedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/votes/10/overview")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.creeParNom").value("Admin Residence"))
                .andExpect(jsonPath("$.finProche").value(true))
                .andExpect(jsonPath("$.currentUserChoice").value("POUR"))
                .andExpect(jsonPath("$.currentUserComment").value("Je valide"));
    }

    @Test
    void getVoteResultReturnsExistingAggregatePayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/votes/10/resultat")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteId").value(10L))
                .andExpect(jsonPath("$.totalPour").value(5))
                .andExpect(jsonPath("$.totalContre").value(2))
                .andExpect(jsonPath("$.totalNeutre").value(1))
                .andExpect(jsonPath("$.statut").value("VALIDE"));
    }
}
