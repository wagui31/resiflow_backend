package com.resiflow.controller;

import com.resiflow.dto.PublicRegistrationCompositionType;
import com.resiflow.dto.PublicRegistrationContextResponse;
import com.resiflow.dto.PublicRegistrationFilterField;
import com.resiflow.dto.PublicRegistrationLogementResponse;
import com.resiflow.dto.PublicRegistrationSearchResponse;
import com.resiflow.entity.TypeLogement;
import com.resiflow.service.LogementService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicRegistrationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LogementService logementService = new LogementService(null, null, null, null) {
            @Override
            public List<PublicRegistrationLogementResponse> getPublicRegistrationLogements(final String residenceCode) {
                return List.of(
                        new PublicRegistrationLogementResponse(
                                15L,
                                TypeLogement.APPARTEMENT,
                                "101",
                                "B",
                                "1",
                                "RES7-APPARTEMENT-B-101",
                                false,
                                0L,
                                3,
                                false
                        ),
                        new PublicRegistrationLogementResponse(
                                16L,
                                TypeLogement.APPARTEMENT,
                                "102",
                                "B",
                                "1",
                                "RES7-APPARTEMENT-B-102",
                                true,
                                3L,
                                3,
                                true
                        )
                );
            }

            @Override
            public PublicRegistrationContextResponse getPublicRegistrationContext(final String residenceCode) {
                return new PublicRegistrationContextResponse(
                        7L,
                        "RES-ABC123",
                        PublicRegistrationCompositionType.APPARTEMENT_ONLY,
                        List.of(PublicRegistrationFilterField.IMMEUBLE, PublicRegistrationFilterField.NUMERO),
                        false,
                        true,
                        2
                );
            }

            @Override
            public PublicRegistrationSearchResponse searchPublicRegistrationLogements(
                    final String residenceCode,
                    final String numero,
                    final String immeuble
            ) {
                return new PublicRegistrationSearchResponse(
                        7L,
                        "RES-ABC123",
                        PublicRegistrationCompositionType.APPARTEMENT_ONLY,
                        List.of(PublicRegistrationFilterField.IMMEUBLE, PublicRegistrationFilterField.NUMERO),
                        numero,
                        immeuble,
                        1,
                        List.of(
                                new PublicRegistrationLogementResponse(
                                        16L,
                                        TypeLogement.APPARTEMENT,
                                        "102",
                                        "B",
                                        "1",
                                        "RES7-APPARTEMENT-B-102",
                                        true,
                                        3L,
                                        3,
                                        true
                                )
                        )
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new PublicRegistrationController(logementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRegistrationLogementsReturnsOccupancyDataForResidenceCode() throws Exception {
        mockMvc.perform(get("/api/public/residences/RES-ABC123/logements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logementId").value(15L))
                .andExpect(jsonPath("$[0].codeInterne").value("RES7-APPARTEMENT-B-101"))
                .andExpect(jsonPath("$[0].occupiedCount").value(0))
                .andExpect(jsonPath("$[0].maxOccupants").value(3))
                .andExpect(jsonPath("$[0].full").value(false))
                .andExpect(jsonPath("$[1].logementId").value(16L))
                .andExpect(jsonPath("$[1].occupiedCount").value(3))
                .andExpect(jsonPath("$[1].full").value(true));
    }

    @Test
    void getRegistrationContextReturnsCompositionAndAllowedFilters() throws Exception {
        mockMvc.perform(get("/api/public/residences/RES-ABC123/registration-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.residenceCode").value("RES-ABC123"))
                .andExpect(jsonPath("$.compositionType").value("APPARTEMENT_ONLY"))
                .andExpect(jsonPath("$.allowedFilters[0]").value("IMMEUBLE"))
                .andExpect(jsonPath("$.allowedFilters[1]").value("NUMERO"))
                .andExpect(jsonPath("$.hasMaison").value(false))
                .andExpect(jsonPath("$.hasAppartement").value(true))
                .andExpect(jsonPath("$.totalLogements").value(2));
    }

    @Test
    void searchRegistrationLogementsReturnsFiltersAndItems() throws Exception {
        mockMvc.perform(get("/api/public/residences/RES-ABC123/logements/search")
                        .param("numero", "102")
                        .param("immeuble", "B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.compositionType").value("APPARTEMENT_ONLY"))
                .andExpect(jsonPath("$.numeroFilter").value("102"))
                .andExpect(jsonPath("$.immeubleFilter").value("B"))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.items[0].logementId").value(16L))
                .andExpect(jsonPath("$.items[0].codeInterne").value("RES7-APPARTEMENT-B-102"));
    }
}
