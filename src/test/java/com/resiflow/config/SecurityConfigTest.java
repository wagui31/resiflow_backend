package com.resiflow.config;

import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.controller.AdminUserController;
import com.resiflow.controller.AuthController;
import com.resiflow.controller.HealthController;
import com.resiflow.controller.ResidenceController;
import com.resiflow.controller.UserController;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.security.JwtAuthenticationFilter;
import com.resiflow.security.JwtProperties;
import com.resiflow.security.JwtService;
import com.resiflow.security.RestAuthenticationEntryPoint;
import com.resiflow.service.AuthService;
import com.resiflow.service.DashboardService;
import com.resiflow.service.PaiementService;
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
    private UserService userService;

    @MockitoBean
    private com.resiflow.service.ResidenceService residenceService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private PaiementService paiementService;

    @MockitoBean
    private StatsService statsService;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void loginEndpointIsPublic() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(4L, "resident@example.com", 12L, UserRole.USER, UserStatus.ACTIVE, "jwt-token"));

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

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"resident@example.com","password":"secret","residenceCode":"RES-ABC123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
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
