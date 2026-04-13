package com.resiflow.controller;

import com.resiflow.dto.DashboardResponse;
import com.resiflow.dto.ExpenseCategoryCountResponse;
import com.resiflow.dto.LogementResponse;
import com.resiflow.dto.LogementSummaryResponse;
import com.resiflow.dto.ResidenceExpenseCategoryStatsResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.ResidencePaymentHousingStatsResponse;
import com.resiflow.dto.ResidenceViewLogementCardResponse;
import com.resiflow.dto.ResidenceViewOverviewResponse;
import com.resiflow.dto.ResidenceViewPendingLogementCardResponse;
import com.resiflow.dto.ResidenceViewPaymentStatusResponse;
import com.resiflow.dto.ResidenceViewResidentResponse;
import com.resiflow.dto.ResidenceViewResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DashboardService;
import com.resiflow.service.DepenseService;
import com.resiflow.service.PaiementService;
import com.resiflow.service.ResidenceService;
import com.resiflow.service.ResidenceViewService;
import com.resiflow.service.StatsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResidenceControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ResidenceService residenceService = new ResidenceService(null);
        DashboardService dashboardService = new DashboardService(null, null, null, null, null) {
            @Override
            public DashboardResponse getDashboard(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return new DashboardResponse(BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO, List.of());
            }
        };
        PaiementService paiementService = new PaiementService(null, null, null, null, null, null, null, null, null) {
            @Override
            public List<ResidenceImpayeResponse> getImpayesByResidence(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(
                        new ResidenceImpayeResponse(
                                4L,
                                new LogementSummaryResponse(4L, "A101", "BAT-A", null, TypeLogement.APPARTEMENT, true),
                                LocalDate.of(2026, 3, 1),
                                26L
                        ),
                        new ResidenceImpayeResponse(
                                9L,
                                new LogementSummaryResponse(9L, "M1", null, null, TypeLogement.MAISON, false),
                                null,
                                null
                        )
                );
            }
        };
        StatsService statsService = new StatsService(null, null, null, null, null, null) {
            @Override
            public StatsResponse getStats(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return new StatsResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
            }

            @Override
            public ResidencePaymentHousingStatsResponse getPaiementLogementStats(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new ResidencePaymentHousingStatsResponse(residenceId, 40L, 31L, 9L);
            }

            @Override
            public ResidenceExpenseCategoryStatsResponse getDepenseCategoryStats(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new ResidenceExpenseCategoryStatsResponse(
                        residenceId,
                        List.of(
                                new ExpenseCategoryCountResponse(1L, "Eau", 5L),
                                new ExpenseCategoryCountResponse(2L, "Electricite", 3L)
                        )
                );
            }
        };
        DepenseService depenseService = new DepenseService(null, null, null, null) {
            @Override
            public Long countActiveParticipants(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return 3L;
            }
        };
        ResidenceViewService residenceViewService = new ResidenceViewService(null, null, null, null, null, null) {
            @Override
            public ResidenceViewResponse getResidenceView(
                    final Long residenceId,
                    final String search,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new ResidenceViewResponse(
                        new ResidenceViewOverviewResponse(
                                residenceId,
                                12L,
                                9L,
                                3L,
                                16L,
                                14L,
                                2L,
                                2L,
                                14L,
                                new BigDecimal("250.00"),
                                "POSITIVE",
                                7L,
                                2L
                        ),
                        List.of(
                                new ResidenceViewLogementCardResponse(
                                        new LogementResponse(
                                                4L,
                                                residenceId,
                                                TypeLogement.APPARTEMENT,
                                                "A101",
                                                "BAT-A",
                                                "10",
                                                "31000",
                                                "Canastel",
                                                "RES7-APPARTEMENT-BAT-A-A101",
                                                true,
                                                null
                                        ),
                                        new com.resiflow.dto.LogementOccupancyResponse(4L, 2L, 3, false),
                                        new ResidenceViewPaymentStatusResponse(
                                                "A_JOUR",
                                                LocalDate.of(2026, 5, 31),
                                                false,
                                                null
                                        ),
                                        List.of(
                                                new ResidenceViewResidentResponse(
                                                        10L,
                                                        "Ali",
                                                        "Admin",
                                                        "ali@example.com",
                                                        UserRole.ADMIN,
                                                        UserStatus.ACTIVE,
                                                        LocalDate.of(2026, 1, 1)
                                                )
                                        )
                                )
                        ),
                        List.of(
                                new ResidenceViewPendingLogementCardResponse(
                                        new LogementResponse(
                                                5L,
                                                residenceId,
                                                TypeLogement.APPARTEMENT,
                                                "A102",
                                                "BAT-A",
                                                "10",
                                                "31000",
                                                "Canastel",
                                                "RES7-APPARTEMENT-BAT-A-A102",
                                                true,
                                                null
                                        ),
                                        new com.resiflow.dto.LogementOccupancyResponse(5L, 1L, 3, false),
                                        List.of(
                                                new ResidenceViewResidentResponse(
                                                        11L,
                                                        "Sara",
                                                        "Resident",
                                                        "sara@example.com",
                                                        UserRole.USER,
                                                        UserStatus.ACTIVE,
                                                        LocalDate.of(2026, 2, 1)
                                                )
                                        ),
                                        List.of(
                                                new ResidenceViewResidentResponse(
                                                        12L,
                                                        "Nora",
                                                        "Pending",
                                                        "nora@example.com",
                                                        UserRole.USER,
                                                        UserStatus.PENDING,
                                                        LocalDate.of(2026, 4, 1)
                                                )
                                        )
                                )
                        )
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ResidenceController(
                                residenceService,
                                dashboardService,
                                paiementService,
                                statsService,
                                depenseService,
                                residenceViewService
                        )
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getImpayesReturnsExpectedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, com.resiflow.entity.UserRole.ADMIN);

        mockMvc.perform(get("/api/residences/7/impayes")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logementId").value(4L))
                .andExpect(jsonPath("$[0].logement.logementId").value(4L))
                .andExpect(jsonPath("$[0].logement.numero").value("A101"))
                .andExpect(jsonPath("$[0].dateFinDernierPaiement").value("2026-03-01"))
                .andExpect(jsonPath("$[0].nombreJoursRetard").value(26))
                .andExpect(jsonPath("$[1].logementId").value(9L))
                .andExpect(jsonPath("$[1].dateFinDernierPaiement").isEmpty())
                .andExpect(jsonPath("$[1].nombreJoursRetard").isEmpty());
    }

    @Test
    void getPaiementsLogementsStatsReturnsExpectedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, com.resiflow.entity.UserRole.ADMIN);

        mockMvc.perform(get("/api/residences/7/stats/paiements-logements")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.totalLogementsActifs").value(40))
                .andExpect(jsonPath("$.logementsAJour").value(31))
                .andExpect(jsonPath("$.logementsEnRetard").value(9));
    }

    @Test
    void getHousingViewReturnsAggregatedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, com.resiflow.entity.UserRole.ADMIN);

        mockMvc.perform(get("/api/residences/7/housing-view")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalLogements").value(12))
                .andExpect(jsonPath("$.overview.activeResidents").value(14))
                .andExpect(jsonPath("$.overview.cagnotteStatus").value("POSITIVE"))
                .andExpect(jsonPath("$.logements[0].logement.numero").value("A101"))
                .andExpect(jsonPath("$.logements[0].payment.status").value("A_JOUR"))
                .andExpect(jsonPath("$.logements[0].residents[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.pendingLogements[0].logement.numero").value("A102"))
                .andExpect(jsonPath("$.pendingLogements[0].existingResidents[0].email").value("sara@example.com"))
                .andExpect(jsonPath("$.pendingLogements[0].pendingResidents[0].status").value("PENDING"));
    }

    @Test
    void getDepensesParCategorieStatsReturnsExpectedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, com.resiflow.entity.UserRole.ADMIN);

        mockMvc.perform(get("/api/residences/7/stats/depenses-par-categorie")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.categories[0].categorieId").value(1L))
                .andExpect(jsonPath("$.categories[0].categorieNom").value("Eau"))
                .andExpect(jsonPath("$.categories[0].nombreDepenses").value(5))
                .andExpect(jsonPath("$.categories[1].categorieId").value(2L))
                .andExpect(jsonPath("$.categories[1].categorieNom").value("Electricite"))
                .andExpect(jsonPath("$.categories[1].nombreDepenses").value(3));
    }
}
