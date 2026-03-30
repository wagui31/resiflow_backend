package com.resiflow.controller;

import com.resiflow.dto.DashboardResponse;
import com.resiflow.dto.ResidenceImpayeResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DashboardService;
import com.resiflow.service.PaiementService;
import com.resiflow.service.ResidenceService;
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
        PaiementService paiementService = new PaiementService(null, null, null, null, null) {
            @Override
            public List<ResidenceImpayeResponse> getImpayesByResidence(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(
                        new ResidenceImpayeResponse(4L, "late1@example.com", LocalDate.of(2026, 3, 1), 26L),
                        new ResidenceImpayeResponse(9L, "late2@example.com", null, null)
                );
            }
        };
        StatsService statsService = new StatsService(null, null, null) {
            @Override
            public StatsResponse getStats(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return new StatsResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ResidenceController(residenceService, dashboardService, paiementService, statsService)
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
                .andExpect(jsonPath("$[0].id").value(4L))
                .andExpect(jsonPath("$[0].email").value("late1@example.com"))
                .andExpect(jsonPath("$[0].dateFinDernierPaiement").value("2026-03-01"))
                .andExpect(jsonPath("$[0].nombreJoursRetard").value(26))
                .andExpect(jsonPath("$[1].id").value(9L))
                .andExpect(jsonPath("$[1].dateFinDernierPaiement").isEmpty())
                .andExpect(jsonPath("$[1].nombreJoursRetard").isEmpty());
    }
}
