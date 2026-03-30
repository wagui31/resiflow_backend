package com.resiflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.service.AuthService;
import com.resiflow.service.CaptchaVerificationService;
import com.resiflow.service.EmailService;
import com.resiflow.service.InvalidCredentialsException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
                new BCryptPasswordEncoder(),
                new EmailService(),
                new CaptchaVerificationService(new com.resiflow.config.CaptchaProperties(false, "", ""), RestClient.builder().build())
        ) {
            @Override
            public LoginResponse login(final LoginRequest request) {
                if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                    throw new IllegalArgumentException("Email must not be blank");
                }
                if ("resident@example.com".equals(request.getEmail().trim())
                        && "secret".equals(request.getPassword().trim())) {
                    return new LoginResponse(1L, "resident@example.com", 7L, UserRole.USER, UserStatus.ACTIVE, TOKEN);
                }
                throw new InvalidCredentialsException("Invalid credentials");
            }

            @Override
            public User register(final RegisterRequest request) {
                if (request == null || request.getResidenceCode() == null || request.getResidenceCode().trim().isEmpty()) {
                    throw new IllegalArgumentException("Residence code must not be blank");
                }

                Residence residence = new Residence();
                residence.setId(7L);
                residence.setCode(request.getResidenceCode().trim());

                User user = new User();
                user.setId(2L);
                user.setEmail(request.getEmail().trim());
                user.setResidence(residence);
                user.setNumeroImmeuble(request.getNumeroImmeuble());
                user.setCodeLogement(request.getCodeLogement());
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.PENDING);
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
                .andExpect(jsonPath("$.message").value("Email must not be blank"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.com");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void registerReturnsPendingUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("resident@example.com");
        request.setPassword("secret");
        request.setResidenceCode("RES-ABC123");
        request.setNumeroImmeuble("B");
        request.setCodeLogement("12A");
        request.setCaptchaToken("captcha-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("resident@example.com"))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.residenceCode").value("RES-ABC123"))
                .andExpect(jsonPath("$.numeroImmeuble").value("B"))
                .andExpect(jsonPath("$.codeLogement").value("12A"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }
}
