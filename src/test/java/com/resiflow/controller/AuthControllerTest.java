package com.resiflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.service.AuthService;
import com.resiflow.service.CaptchaVerificationService;
import com.resiflow.service.EmailAlreadyUsedException;
import com.resiflow.service.InvalidCredentialsException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private static final String TOKEN = "jwt-token";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthService authService = new AuthService(
                null,
                null,
                null,
                null,
                new BCryptPasswordEncoder(),
                new CaptchaVerificationService(new com.resiflow.config.CaptchaProperties(false, "", "", ""), RestClient.builder().build()),
                noOpEventPublisher()
        ) {
            @Override
            public LoginResponse login(final LoginRequest request) {
                if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                    throw new IllegalArgumentException("Email must not be blank");
                }
                if ("resident@example.com".equals(request.getEmail().trim())
                        && "secret".equals(request.getPassword().trim())) {
                    return new LoginResponse(1L, "resident@example.com", 7L, "EUR", UserRole.USER, UserStatus.ACTIVE, TOKEN);
                }
                throw new InvalidCredentialsException("Invalid credentials");
            }

            @Override
            public User register(final RegisterRequest request, final String clientPlatform) {
                if (request == null || request.getResidenceCode() == null || request.getResidenceCode().trim().isEmpty()) {
                    throw new IllegalArgumentException("Residence code must not be blank");
                }
                if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                    throw new IllegalArgumentException("First name must not be blank");
                }
                if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Last name must not be blank");
                }
                if (request.getLogementId() == null) {
                    throw new IllegalArgumentException("Logement ID must not be null");
                }
                if ("duplicate@example.com".equals(request.getEmail().trim())) {
                    throw new EmailAlreadyUsedException("Email is already used");
                }

                Residence residence = new Residence();
                residence.setId(7L);
                residence.setCode(request.getResidenceCode().trim());
                residence.setCurrency("EUR");

                Logement logement = new Logement();
                logement.setId(request.getLogementId());
                logement.setNumero("12A");
                logement.setImmeuble("B");
                logement.setTypeLogement(TypeLogement.APPARTEMENT);
                logement.setActive(Boolean.FALSE);
                logement.setResidence(residence);

                User user = new User();
                user.setId(2L);
                user.setEmail(request.getEmail().trim());
                user.setFirstName(request.getFirstName().trim());
                user.setLastName(request.getLastName().trim());
                user.setResidence(residence);
                user.setLogement(logement);
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.PENDING);
                user.setDateEntreeResidence(LocalDate.of(2026, 4, 11));
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                return user;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ApplicationEventPublisher noOpEventPublisher() {
        return event -> {
        };
    }

    @Test
    void loginReturnsUserDataWhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword("secret");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("resident@example.com"))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void loginReturnsBadRequestWhenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(" ");
        request.setPassword("secret");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Email must not be blank"));
    }

    @Test
    void registerReturnsPendingUserWithLogementSummary() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("resident@example.com");
        request.setFirstName("Lea");
        request.setLastName("Martin");
        request.setPassword("secret");
        request.setResidenceCode("RES-ABC123");
        request.setLogementId(15L);
        request.setCaptchaToken("captcha-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("resident@example.com"))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.residenceCode").value("RES-ABC123"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.logement.logementId").value(15L))
                .andExpect(jsonPath("$.logement.numero").value("12A"))
                .andExpect(jsonPath("$.logement.immeuble").value("B"))
                .andExpect(jsonPath("$.logement.typeLogement").value("APPARTEMENT"))
                .andExpect(jsonPath("$.logement.active").value(false))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
