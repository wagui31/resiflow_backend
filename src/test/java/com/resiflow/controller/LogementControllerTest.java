package com.resiflow.controller;

import com.resiflow.dto.CreateLogementsBulkResponse;
import com.resiflow.dto.LogementResponse;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.UserRole;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.LogementService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogementControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LogementService logementService = new LogementService(null, null, null, null) {
            @Override
            public CreateLogementsBulkResponse createLogementsBulk(
                    final com.resiflow.dto.CreateLogementsBulkRequest request,
                    final AuthenticatedUser authenticatedUser
            ) {
                return new CreateLogementsBulkResponse(
                        2,
                        List.of(
                                new LogementResponse(
                                        1L,
                                        7L,
                                        TypeLogement.MAISON,
                                        "001",
                                        null,
                                        "0",
                                        "31000",
                                        "Canastel Belgaid, ORAN",
                                        "RES7-MAISON-001",
                                        false,
                                        null
                                ),
                                new LogementResponse(
                                        2L,
                                        7L,
                                        TypeLogement.MAISON,
                                        "002",
                                        null,
                                        "0",
                                        "31000",
                                        "Canastel Belgaid, ORAN",
                                        "RES7-MAISON-002",
                                        false,
                                        null
                                )
                        )
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new LogementController(logementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createLogementsBulkReturnsCreatedLogements() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        mockMvc.perform(post("/api/logements/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "residenceId": 7,
                                  "typeLogement": "MAISON",
                                  "numeroDebut": "001",
                                  "numeroFin": "002",
                                  "etage": "0",
                                  "codePostal": "31000",
                                  "adresse": "Canastel Belgaid, ORAN"
                                }
                                """)
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdCount").value(2))
                .andExpect(jsonPath("$.logements[0].numero").value("001"))
                .andExpect(jsonPath("$.logements[0].codeInterne").value("RES7-MAISON-001"))
                .andExpect(jsonPath("$.logements[1].numero").value("002"));
    }
}
