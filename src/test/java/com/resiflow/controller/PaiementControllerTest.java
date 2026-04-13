package com.resiflow.controller;

import com.resiflow.dto.CreateMyPaiementRequest;
import com.resiflow.dto.CreatePaiementRequest;
import com.resiflow.dto.LogementSummaryResponse;
import com.resiflow.dto.PaymentHistoryItemResponse;
import com.resiflow.dto.PaymentStatusMonthResponse;
import com.resiflow.dto.PaymentStatusTimelineResponse;
import com.resiflow.dto.PendingPaymentResponse;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PaiementService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaiementControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaiementService paiementService = new PaiementService(null, null, null, null, null, null, null, null, null) {
            @Override
            public Paiement createPaiement(final CreatePaiementRequest request, final AuthenticatedUser authenticatedUser) {
                return buildPaiement(42L, PaiementStatus.PENDING, 15L);
            }

            @Override
            public Paiement createMyPaiement(final CreateMyPaiementRequest request, final AuthenticatedUser authenticatedUser) {
                return buildPaiement(43L, PaiementStatus.PENDING, 15L);
            }

            @Override
            public Paiement createAdminUserPaiementByEmail(
                    final String email,
                    final CreateMyPaiementRequest request,
                    final AuthenticatedUser authenticatedUser
            ) {
                return buildPaiement(44L, PaiementStatus.PENDING, 15L);
            }

            @Override
            public Paiement validatePaiement(final Long paiementId, final AuthenticatedUser authenticatedUser) {
                return buildPaiement(paiementId, PaiementStatus.VALIDATED, 15L);
            }

            @Override
            public PaymentStatusTimelineResponse getMyPaymentStatus(final AuthenticatedUser authenticatedUser) {
                return new PaymentStatusTimelineResponse(
                        "OVERDUE",
                        LocalDate.of(2026, 6, 30),
                        true,
                        new PendingPaymentResponse(9L, new BigDecimal("18000.00"), 3, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30)),
                        List.of(
                                new PaymentStatusMonthResponse("2026-01", false),
                                new PaymentStatusMonthResponse("2026-02", false),
                                new PaymentStatusMonthResponse("2026-03", true)
                        ),
                        List.of(
                                new PaymentHistoryItemResponse(
                                        LocalDate.of(2026, 3, 1),
                                        new BigDecimal("6000.00"),
                                        "2026-01 - 2026-03"
                                )
                        )
                );
            }

            @Override
            public PaymentStatusTimelineResponse getAdminUserPaymentStatusByEmail(
                    final String email,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new PaymentStatusTimelineResponse(
                        "UP_TO_DATE",
                        LocalDate.of(2026, 9, 30),
                        false,
                        null,
                        List.of(
                                new PaymentStatusMonthResponse("2026-07", true),
                                new PaymentStatusMonthResponse("2026-08", true),
                                new PaymentStatusMonthResponse("2026-09", true)
                        ),
                        List.of(
                                new PaymentHistoryItemResponse(
                                        LocalDate.of(2026, 7, 1),
                                        new BigDecimal("18000.00"),
                                        "2026-07 - 2026-09"
                                )
                        )
                );
            }

            @Override
            public PaymentStatusTimelineResponse getAdminLogementPaymentStatus(
                    final Long logementId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new PaymentStatusTimelineResponse(
                        "UP_TO_DATE",
                        LocalDate.of(2026, 12, 31),
                        false,
                        null,
                        List.of(
                                new PaymentStatusMonthResponse("2026-10", true),
                                new PaymentStatusMonthResponse("2026-11", true),
                                new PaymentStatusMonthResponse("2026-12", true)
                        ),
                        List.of(
                                new PaymentHistoryItemResponse(
                                        LocalDate.of(2026, 10, 1),
                                        new BigDecimal("18000.00"),
                                        "2026-10 - 2026-12"
                                )
                        )
                );
            }

            @Override
            public List<Paiement> getPendingPaiementsForAdmin(final AuthenticatedUser authenticatedUser) {
                return List.of(buildPaiement(45L, PaiementStatus.PENDING, 15L));
            }

            private Paiement buildPaiement(final Long paiementId, final PaiementStatus status, final Long logementId) {
                Residence residence = new Residence();
                residence.setId(7L);

                Logement logement = new Logement();
                logement.setId(logementId);
                logement.setNumero("A101");
                logement.setImmeuble("BAT-A");
                logement.setTypeLogement(TypeLogement.APPARTEMENT);
                logement.setActive(Boolean.TRUE);
                logement.setResidence(residence);

                User creator = new User();
                creator.setId(2L);
                creator.setFirstName("Admin");
                creator.setLastName("Residence");

                Paiement paiement = new Paiement();
                paiement.setId(paiementId);
                paiement.setLogement(logement);
                paiement.setResidence(residence);
                paiement.setNombreMois(3);
                paiement.setMontantMensuel(new BigDecimal("6000.00"));
                paiement.setMontantTotal(new BigDecimal("18000.00"));
                paiement.setDateDebut(LocalDate.of(2026, 1, 1));
                paiement.setDateFin(LocalDate.of(2026, 3, 31));
                paiement.setDatePaiement(LocalDateTime.of(2026, 1, 5, 10, 15));
                paiement.setCreePar(creator);
                paiement.setStatus(status);
                paiement.setTypePaiement(TypePaiement.CAGNOTTE);
                return paiement;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new PaiementController(paiementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPaiementReturnsPendingPaiement() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(post("/api/paiements")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"logementId":15,"residenceId":7,"nombreMois":3,"dateDebut":"2026-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.logementId").value(15L))
                .andExpect(jsonPath("$.dateFin").value("2026-03-31"));
    }

    @Test
    void validatePaiementReturnsValidatedPaiement() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(put("/api/paiements/42/validate")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATED"))
                .andExpect(jsonPath("$.logementId").value(15L))
                .andExpect(jsonPath("$.dateFin").value("2026-03-31"));
    }

    @Test
    void createMyPaiementReturnsPendingPaiementForAuthenticatedUser() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(post("/api/payments/me")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreMois":2,"dateDebut":"2026-04-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(43L))
                .andExpect(jsonPath("$.logementId").value(15L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPendingPaiementsForAdminReturnsCreatorIdentity() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(get("/api/paiements/admin/pending")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(45L))
                .andExpect(jsonPath("$[0].creeParId").value(2L))
                .andExpect(jsonPath("$[0].creePar.id").value(2L))
                .andExpect(jsonPath("$[0].creePar.firstName").value("Admin"))
                .andExpect(jsonPath("$[0].creePar.lastName").value("Residence"));
    }

    @Test
    void getAdminUserPaymentStatusReturnsTimelineForResidentEmail() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(get("/api/payments/admin/user/status")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null))
                        .param("email", "resident@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP_TO_DATE"))
                .andExpect(jsonPath("$.dateFin").value("2026-09-30"))
                .andExpect(jsonPath("$.nextDueWarning").value(false))
                .andExpect(jsonPath("$.pendingPayment").doesNotExist())
                .andExpect(jsonPath("$.months[0].month").value("2026-07"))
                .andExpect(jsonPath("$.months[0].paid").value(true));
    }

    @Test
    void getAdminLogementPaymentStatusReturnsTimelineForLogement() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(get("/api/payments/admin/logement/15/status")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP_TO_DATE"))
                .andExpect(jsonPath("$.dateFin").value("2026-12-31"))
                .andExpect(jsonPath("$.months[0].month").value("2026-10"))
                .andExpect(jsonPath("$.months[0].paid").value(true));
    }
}
