package com.resiflow.controller;

import com.resiflow.dto.CreateCorrectionCagnotteRequest;
import com.resiflow.dto.CreateCorrectionCagnotteResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.entity.UserRole;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.CagnotteService;
import com.resiflow.service.CorrectionCagnotteService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CagnotteControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CagnotteService cagnotteService = new CagnotteService(null, null) {
            @Override
            public BigDecimal calculerSolde(final Long residenceId, final AuthenticatedUser authenticatedUser) {
                return new BigDecimal("240.00");
            }

            @Override
            public List<TransactionCagnotte> getTransactions(
                    final Long residenceId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(
                        buildContributionTransaction(),
                        buildExpenseTransaction(),
                        buildCorrectionTransaction()
                );
            }
        };

        CorrectionCagnotteService correctionCagnotteService = new CorrectionCagnotteService(null, null, null, null) {
            @Override
            public CreateCorrectionCagnotteResponse createCorrection(
                    final Long residenceId,
                    final CreateCorrectionCagnotteRequest request,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new CreateCorrectionCagnotteResponse(
                        residenceId,
                        new BigDecimal("240.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("60.00"),
                        55L,
                        99L,
                        TypeTransactionCagnotte.CORRECTION,
                        LocalDateTime.of(2026, 4, 19, 14, 32)
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new CagnotteController(cagnotteService, correctionCagnotteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTransactionsReturnsResidenceFundMovementsWithHousingCodeWhenAvailable() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/cagnotte/7/transactions")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("CONTRIBUTION"))
                .andExpect(jsonPath("$[0].logementId").value(15L))
                .andExpect(jsonPath("$[0].logementCodeInterne").value("RES7-APPARTEMENT-B-101"))
                .andExpect(jsonPath("$[0].montant").value(120.00))
                .andExpect(jsonPath("$[0].dateCreation[0]").value(2026))
                .andExpect(jsonPath("$[0].dateCreation[1]").value(4))
                .andExpect(jsonPath("$[0].dateCreation[2]").value(12))
                .andExpect(jsonPath("$[0].dateCreation[3]").value(9))
                .andExpect(jsonPath("$[0].dateCreation[4]").value(0))
                .andExpect(jsonPath("$[1].type").value("DEPENSE"))
                .andExpect(jsonPath("$[1].logementId").doesNotExist())
                .andExpect(jsonPath("$[1].logementCodeInterne").doesNotExist())
                .andExpect(jsonPath("$[1].montant").value(80.00))
                .andExpect(jsonPath("$[1].dateCreation[0]").value(2026))
                .andExpect(jsonPath("$[1].dateCreation[1]").value(4))
                .andExpect(jsonPath("$[1].dateCreation[2]").value(10))
                .andExpect(jsonPath("$[1].dateCreation[3]").value(15))
                .andExpect(jsonPath("$[1].dateCreation[4]").value(30));
    }

    @Test
    void createCorrectionReturnsCorrectionPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(post("/api/cagnotte/7/corrections")
                        .contentType("application/json")
                        .content("""
                                {"nouveauSolde":300.00,"motif":"Correction suite a erreur"}
                                """)
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.ancienSolde").value(240.00))
                .andExpect(jsonPath("$.nouveauSolde").value(300.00))
                .andExpect(jsonPath("$.delta").value(60.00))
                .andExpect(jsonPath("$.correctionId").value(55L))
                .andExpect(jsonPath("$.transactionId").value(99L))
                .andExpect(jsonPath("$.typeTransaction").value("CORRECTION"));
    }

    @Test
    void getSoldeReturnsResidenceBalance() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/cagnotte/7/solde")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.solde").value(240.00));
    }

    private TransactionCagnotte buildContributionTransaction() {
        Residence residence = new Residence();
        residence.setId(7L);

        Logement logement = new Logement();
        logement.setId(15L);
        logement.setCodeInterne("RES7-APPARTEMENT-B-101");

        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setId(42L);
        transaction.setResidence(residence);
        transaction.setLogement(logement);
        transaction.setType(TypeTransactionCagnotte.CONTRIBUTION);
        transaction.setMontant(new BigDecimal("120.00"));
        transaction.setReferenceId(1001L);
        transaction.setDateCreation(LocalDateTime.of(2026, 4, 12, 9, 0));
        return transaction;
    }

    private TransactionCagnotte buildExpenseTransaction() {
        Residence residence = new Residence();
        residence.setId(7L);

        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setId(43L);
        transaction.setResidence(residence);
        transaction.setLogement(null);
        transaction.setType(TypeTransactionCagnotte.DEPENSE);
        transaction.setMontant(new BigDecimal("80.00"));
        transaction.setReferenceId(1002L);
        transaction.setDateCreation(LocalDateTime.of(2026, 4, 10, 15, 30));
        return transaction;
    }

    private TransactionCagnotte buildCorrectionTransaction() {
        Residence residence = new Residence();
        residence.setId(7L);

        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setId(44L);
        transaction.setResidence(residence);
        transaction.setLogement(null);
        transaction.setType(TypeTransactionCagnotte.CORRECTION);
        transaction.setMontant(new BigDecimal("-20.00"));
        transaction.setReferenceId(1003L);
        transaction.setDateCreation(LocalDateTime.of(2026, 4, 9, 8, 15));
        return transaction;
    }
}
