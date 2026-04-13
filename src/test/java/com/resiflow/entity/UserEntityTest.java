package com.resiflow.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEntityTest {

    @Test
    void prePersistAllowsSuperAdminWithoutResidenceAndLogement() {
        User user = new User();
        user.setEmail("superadmin@example.com");
        user.setPassword("secret");
        user.setRole(UserRole.SUPER_ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        assertThatCode(user::prePersist).doesNotThrowAnyException();
    }

    @Test
    void prePersistRejectsRegularUserWithoutResidenceAndLogement() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        assertThatThrownBy(user::prePersist)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Residence is required for non-super-admin users");
    }
}
