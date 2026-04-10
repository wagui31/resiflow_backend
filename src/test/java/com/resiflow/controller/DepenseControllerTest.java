package com.resiflow.controller;

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
                return List.of(
                        buildDepense(10L, TypeDepense.CAGNOTTE, StatutDepense.APPROUVEE, LocalDateTime.of(2026, 4, 10, 9, 0))
                );
            }
        };

        PaiementService paiementService = new PaiementService(null, null, null, null, null);

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
