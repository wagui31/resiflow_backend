package com.resiflow.dto;

import com.resiflow.entity.UserRole;

public class UpdateUserRoleRequest {

    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(final UserRole role) {
        this.role = role;
    }
}
