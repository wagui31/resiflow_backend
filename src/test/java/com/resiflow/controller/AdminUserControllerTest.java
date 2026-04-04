package com.resiflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resiflow.dto.UpdateUserRoleRequest;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.UserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserService userService = new UserService(null, new BCryptPasswordEncoder(), null, null) {
            @Override
            public User updateUserRole(
                    final Long userId,
                    final AuthenticatedUser authenticatedUser,
                    final UserRole role
            ) {
                if (userId.equals(authenticatedUser.userId())) {
                    throw new IllegalStateException("Cannot change your own role");
                }
                if (role == null) {
                    throw new IllegalArgumentException("Role must not be null");
                }

                User user = new User();
                user.setId(userId);
                user.setEmail("target@example.com");
                user.setResidenceId(authenticatedUser.residenceId());
                user.setRole(role);
                user.setStatus(UserStatus.ACTIVE);
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now.minusDays(1));
                user.setUpdatedAt(now);
                return user;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateUserRoleReturnsUpdatedUser() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN);
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.ADMIN);

        mockMvc.perform(put("/api/admin/users/14/role")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(14L))
                .andExpect(jsonPath("$.residenceId").value(7L))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateUserRoleReturnsBadRequestWhenTryingToChangeOwnRole() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(10L, "admin@example.com", 7L, UserRole.ADMIN);
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.USER);

        mockMvc.perform(put("/api/admin/users/10/role")
                        .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Cannot change your own role"));
    }
}
