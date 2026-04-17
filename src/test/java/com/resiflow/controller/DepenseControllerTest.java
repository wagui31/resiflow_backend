package com.resiflow.controller;

import com.resiflow.dto.DepenseContributionLogementResponse;
import com.resiflow.dto.ExpenseUserSummaryResponse;
import com.resiflow.dto.SharedExpenseParticipantResponse;
import com.resiflow.dto.SharedExpenseSummaryResponse;
import com.resiflow.entity.CategorieDepense;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutDepense;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.DepenseService;
import com.resiflow.service.PaiementService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepenseControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DepenseService depenseService = new DepenseService(null, null, null, null) {
            @Override
            public List<Depense> getDepensesByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return List.of(
                        buildDepense(10L, TypeDepense.CAGNOTTE, StatutDepense.APPROUVEE, LocalDateTime.of(2026, 4, 10, 9, 0)),
                        buildDepense(11L, TypeDepense.PARTAGE, StatutDepense.EN_ATTENTE, LocalDateTime.of(2026, 4, 9, 9, 0))
                );
            }

            @Override
            public List<Depense> getApprovedCagnotteDepensesByResidence(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(buildDepense(10L, TypeDepense.CAGNOTTE, StatutDepense.APPROUVEE, LocalDateTime.of(2026, 4, 10, 9, 0)));
            }

            @Override
            public List<SharedExpenseSummaryResponse> getApprovedSharedDepenseSummariesByResidence(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(new SharedExpenseSummaryResponse(
                        11L,
                        7L,
                        5L,
                        "Entretien",
                        "Menage commun",
                        new BigDecimal("120.00"),
                        new BigDecimal("80.00"),
                        new BigDecimal("40.00"),
                        1,
                        LocalDateTime.of(2026, 4, 9, 9, 0),
                        LocalDateTime.of(2026, 4, 10, 10, 0),
                        new ExpenseUserSummaryResponse(2L, "Admin", "Residence", "Admin Residence"),
                        List.of(
                                new SharedExpenseParticipantResponse(
                                        15L,
                                        "B - 101",
                                        "RES7-APPARTEMENT-B-101",
                                        new BigDecimal("40.00"),
                                        new BigDecimal("40.00"),
                                        "PAYE"
                                ),
                                new SharedExpenseParticipantResponse(
                                        16L,
                                        "B - 102",
                                        "RES7-APPARTEMENT-B-102",
                                        new BigDecimal("40.00"),
                                        new BigDecimal("0.00"),
                                        "NON_PAYE"
                                )
                        )
                ));
            }

            @Override
            public List<DepenseContributionLogementResponse> getDepenseContributions(
                    final Long depenseId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(
                        new DepenseContributionLogementResponse(
                                15L,
                                "B - 101",
                                "RES7-APPARTEMENT-B-101",
                                new BigDecimal("40.00"),
                                new BigDecimal("20.00"),
                                "PARTIELLEMENT_PAYE"
                        )
                );
            }

            @Override
            public void softDeleteSharedDepense(final Long depenseId, final AuthenticatedUser authenticatedUser) {
            }
        };

        PaiementService paiementService = new PaiementService(null, null, null, null, null, null, null, null, null);

        mockMvc = MockMvcBuilders.standaloneSetup(new DepenseController(depenseService, paiementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDepensesByResidenceReturnsAllExpenseTypes() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(get("/api/depenses/residence/7")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].typeDepense").value("CAGNOTTE"))
                .andExpect(jsonPath("$[1].typeDepense").value("PARTAGE"));
    }

    @Test
    void getApprovedCagnotteDepensesByResidenceReturnsOnlyApprovedCagnotteExpenses() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/depenses/residence/7/cagnotte/approuvees")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].typeDepense").value("CAGNOTTE"))
                .andExpect(jsonPath("$[0].statut").value("APPROUVEE"));
    }

    @Test
    void getApprovedSharedDepensesByResidenceReturnsSharedSummariesByLogement() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/depenses/residence/7/partagees/approuvees")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11L))
                .andExpect(jsonPath("$[0].montantPayeTotal").value(80.00))
                .andExpect(jsonPath("$[0].participants[0].logementId").value(15L))
                .andExpect(jsonPath("$[0].participants[0].logementLabel").value("B - 101"))
                .andExpect(jsonPath("$[0].participants[0].codeInterne").value("RES7-APPARTEMENT-B-101"));
    }

    @Test
    void getDepenseContributionsReturnsLogementBasedContract() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/depenses/11/contributions")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logementId").value(15L))
                .andExpect(jsonPath("$[0].logementLabel").value("B - 101"))
                .andExpect(jsonPath("$[0].codeInterne").value("RES7-APPARTEMENT-B-101"))
                .andExpect(jsonPath("$[0].statut").value("PARTIELLEMENT_PAYE"));
    }

    @Test
    void deleteSharedDepenseReturnsNoContent() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(delete("/api/depenses/11")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isNoContent());
    }

    private Depense buildDepense(
            final Long id,
            final TypeDepense typeDepense,
            final StatutDepense statut,
            final LocalDateTime dateCreation
    ) {
        Residence residence = new Residence();
        residence.setId(7L);

        User creator = new User();
        creator.setId(2L);

        User validator = new User();
        validator.setId(3L);

        CategorieDepense categorie = new CategorieDepense();
        categorie.setId(5L);
        categorie.setNom("Entretien");

        Depense depense = new Depense();
        depense.setId(id);
        depense.setResidence(residence);
        depense.setCategorie(categorie);
        depense.setMontant(new BigDecimal("250.50"));
        depense.setTypeDepense(typeDepense);
        depense.setDescription("Remplacement ampoules hall");
        depense.setStatut(statut);
        depense.setCreePar(creator);
        depense.setDateCreation(dateCreation);
        depense.setValidePar(validator);
        depense.setDateValidation(LocalDateTime.of(2026, 4, 10, 10, 0));
        return depense;
    }
}
