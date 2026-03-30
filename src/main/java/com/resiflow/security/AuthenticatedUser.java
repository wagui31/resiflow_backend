package com.resiflow.security;

import com.resiflow.entity.UserRole;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthenticatedUser(Long userId, String email, Long residenceId, UserRole role) {

    public List<SimpleGrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
