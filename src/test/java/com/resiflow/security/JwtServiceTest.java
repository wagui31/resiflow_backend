package com.resiflow.security;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "Zm9yLXRlc3RzLW9ubHktcmVzaWZsb3ctand0LXNlY3JldC1rZXktMzItYnl0ZXM=";

    @Test
    void generateTokenReturnsTokenWithExpectedClaims() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3600000));

        User user = new User();
        user.setId(4L);
        user.setEmail("resident@example.com");
        user.setResidenceId(12L);
        user.setRole(UserRole.USER);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("resident@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(4L);
        assertThat(jwtService.extractResidenceId(token)).isEqualTo(12L);
        assertThat(jwtService.extractRole(token)).isEqualTo(UserRole.USER);
    }

    @Test
    void isTokenValidRejectsMalformedToken() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3600000));

        assertThatThrownBy(() -> jwtService.isTokenValid("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }
}
