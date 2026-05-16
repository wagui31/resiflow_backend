package com.resiflow.config;

import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.controller.AdminUserController;
import com.resiflow.controller.AuthController;
import com.resiflow.controller.HealthController;
import com.resiflow.controller.PublicRegistrationController;
import com.resiflow.controller.ResidenceController;
import com.resiflow.controller.UserController;
import com.resiflow.dto.PublicRegistrationCompositionType;
import com.resiflow.dto.PublicRegistrationContextResponse;
import com.resiflow.dto.PublicRegistrationFilterField;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.security.JwtAuthenticationFilter;
import com.resiflow.security.JwtProperties;
import com.resiflow.security.JwtService;
import com.resiflow.security.RestAuthenticationEntryPoint;
import com.resiflow.service.AuthService;
import com.resiflow.service.DashboardService;
import com.resiflow.service.DepenseService;
import com.resiflow.service.ForgotPasswordService;
import com.resiflow.service.LogementService;
import com.resiflow.service.PaiementService;
import com.resiflow.service.ResidenceAccessService;
import com.resiflow.service.ResidenceViewService;
import com.resiflow.service.StatsService;
import com.resiflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        HealthController.class,
        AuthController.class,
        UserController.class,
        AdminUserController.class,
        PublicRegistrationController.class,
        ResidenceController.class,
        SecurityConfigTest.TestProtectedController.class
})
@Import({
        SecurityConfig.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        SecurityConfigTest.TestProtectedController.class,
        SecurityConfigTest.SecurityTestConfiguration.class
})
class SecurityConfigTest {

    private static final String SECRET = "Zm9yLXRlc3RzLW9ubHktcmVzaWZsb3ctand0LXNlY3JldC1rZXktMzItYnl0ZXM=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ForgotPasswordService forgotPasswordService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.resiflow.service.ResidenceService residenceService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private PaiementService paiementService;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    private DepenseService depenseService;

    @MockitoBean
    private ResidenceViewService residenceViewService;

    @MockitoBean
    private ResidenceAccessService residenceAccessService;

    @MockitoBean
    private LogementService logementService;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void loginEndpointIsPublic() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(4L, "resident@example.com", 12L, "EUR", UserRole.USER, UserStatus.ACTIVE, "jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"resident@example.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerEndpointIsPublic() throws Exception {
        User user = new User();
        user.setEmail("resident@example.com");
        user.setId(5L);
        user.setResidenceId(7L);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        when(authService.register(any(RegisterRequest.class), any())).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"resident@example.com","password":"secret","residenceCode":"RES-ABC123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void forgotPasswordRequestCodeEndpointIsPublic() throws Exception {
        when(forgotPasswordService.requestCode(any()))
                .thenReturn(new com.resiflow.dto.ForgotPasswordRequestCodeResponse(
                        "Si un compte existe pour cet email, un code de reinitialisation a ete envoye."
                ));

        mockMvc.perform(post("/api/auth/forgot-password/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"resident@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void publicRegistrationContextEndpointIsPublic() throws Exception {
        when(logementService.getPublicRegistrationContext("RES-ABC123"))
                .thenReturn(new PublicRegistrationContextResponse(
                        7L,
                        "RES-ABC123",
                        PublicRegistrationCompositionType.MIXED,
                        java.util.List.of(PublicRegistrationFilterField.IMMEUBLE, PublicRegistrationFilterField.NUMERO),
                        true,
                        true,
                        5
                ));

        mockMvc.perform(get("/api/public/residences/RES-ABC123/registration-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositionType").value("MIXED"));
    }

    @Test
    void publicRegistrationSearchEndpointIsPublic() throws Exception {
        when(logementService.searchPublicRegistrationLogements("RES-ABC123", "001", "A"))
                .thenReturn(new com.resiflow.dto.PublicRegistrationSearchResponse(
                        7L,
                        "RES-ABC123",
                        PublicRegistrationCompositionType.MIXED,
                        java.util.List.of(PublicRegistrationFilterField.IMMEUBLE, PublicRegistrationFilterField.NUMERO),
                        "001",
                        "A",
                        0,
                        java.util.List.of()
                ));

        mockMvc.perform(get("/api/public/residences/RES-ABC123/logements/search")
                        .param("numero", "001")
                        .param("immeuble", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroFilter").value("001"))
                .andExpect(jsonPath("$.immeubleFilter").value("A"));
    }

    @Test
    void protectedEndpointReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void createAdminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"secret","residenceId":7}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void protectedEndpointAcceptsValidBearerToken() throws Exception {
        User user = new User();
        user.setId(4L);
        user.setEmail("resident@example.com");
        user.setResidenceId(12L);
        user.setRole(UserRole.USER);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("protected"));
    }

    @Test
    void pendingUsersEndpointAcceptsAdminBearerToken() throws Exception {
        User user = new User();
        user.setId(4L);
        user.setEmail("admin@example.com");
        user.setResidenceId(12L);
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        String token = jwtService.generateToken(user);
        when(userService.getPendingUsers(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestProtectedController {

        @GetMapping("/protected")
        String protectedEndpoint() {
            return "protected";
        }
    }

    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class SecurityTestConfiguration {
    }
}
