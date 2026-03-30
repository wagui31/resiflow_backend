package com.resiflow.dto;

import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;

public class LoginResponse {

    private final Long userId;
    private final String email;
    private final Long residenceId;
    private final UserRole role;
    private final UserStatus status;
    private final String token;

    public LoginResponse(
            final Long userId,
            final String email,
            final Long residenceId,
            final UserRole role,
            final UserStatus status,
            final String token
    ) {
        this.userId = userId;
        this.email = email;
        this.residenceId = residenceId;
        this.role = role;
        this.status = status;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }
}
