package com.resiflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resiflow.dto.CreateAdminRequest;
import com.resiflow.dto.UserPaiementHistoryResponse;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PaiementService;
import com.resiflow.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserService userService = new UserService(null, new BCryptPasswordEncoder(), null, null) {
            @Override
            public User createAdmin(final CreateAdminRequest request) {
                if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                    throw new IllegalArgumentException("Email must not be blank");
                }

                User user = new User();
                user.setId(1L);
                user.setEmail(request.getEmail().trim());
                user.setPassword(request.getPassword().trim());
                user.setResidenceId(request.getResidenceId());
                user.setRole(UserRole.ADMIN);
                user.setStatus(UserStatus.ACTIVE);
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                return user;
            }

            @Override
            public User getCurrentUser(final AuthenticatedUser authenticatedUser) {
                Residence residence = new Residence();
                residence.setId(authenticatedUser.residenceId());
                residence.setCurrency("EUR");

                User user = new User();
                user.setId(authenticatedUser.userId());
                user.setEmail(authenticatedUser.email());
                user.setResidence(residence);
                user.setRole(authenticatedUser.role());
                user.setStatus(UserStatus.ACTIVE);
                user.setStatutPaiement(StatutPaiement.A_JOUR);
                user.setNumeroImmeuble("B");
                user.setCodeLogement("12A");
                user.setDateEntreeResidence(LocalDate.of(2026, 1, 15));
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                return user;
            }
        };

        PaiementService paiementService = new PaiementService(null, null, null, null, null) {
            @Override
            public List<UserPaiementHistoryResponse> getPaiementHistoryByUtilisateur(
                    final Long userId,
                    final AuthenticatedUser authenticatedUser
            ) {
                return List.of(
                        new UserPaiementHistoryResponse(
                                LocalDate.of(2026, 3, 1),
                                LocalDate.of(2026, 3, 31),
                                new BigDecimal("150.00"),
                                PaiementStatus.VALIDATED
                        ),
                        new UserPaiementHistoryResponse(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 2, 28),
                                new BigDecimal("150.00"),
                                PaiementStatus.PENDING
                        )
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, paiementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAdminReturnsCreatedUser() throws Exception {
        CreateAdminRequest request = new CreateAdminRequest();
        request.setEmail("admin@example.com");
        request.setPassword("secret");
        request.setResidenceId(7L);

        mockMvc.perform(post("/api/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void createAdminReturnsBadRequestWhenServiceRejectsRequest() throws Exception {
        CreateAdminRequest request = new CreateAdminRequest();
        request.setEmail(" ");
        request.setPassword("secret");
        request.setResidenceId(7L);

        mockMvc.perform(post("/api/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Email must not be blank"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUserPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.statutPaiement").value("A_JOUR"))
                .andExpect(jsonPath("$.numeroImmeuble").value("B"))
                .andExpect(jsonPath("$.codeLogement").value("12A"))
                .andExpect(jsonPath("$.dateEntreeResidence").value("2026-01-15"));
    }

    @Test
    void getPaiementHistoryReturnsReducedPayload() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "user@example.com", 7L, UserRole.USER);

        mockMvc.perform(get("/api/users/10/paiements")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dateDebut").value("2026-03-01"))
                .andExpect(jsonPath("$[0].dateFin").value("2026-03-31"))
                .andExpect(jsonPath("$[0].montantTotal").value(150.00))
                .andExpect(jsonPath("$[0].status").value("VALIDATED"))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].datePaiement").doesNotExist())
                .andExpect(jsonPath("$[1].status").value("PENDING"));
    }
}
