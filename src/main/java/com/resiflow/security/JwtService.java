package com.resiflow.security;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtService(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    public String generateToken(final User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("residenceId", user.getResidenceId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(final String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(final String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public Long extractResidenceId(final String token) {
        return parseClaims(token).get("residenceId", Long.class);
    }

    public UserRole extractRole(final String token) {
        return UserRole.valueOf(parseClaims(token).get("role", String.class));
    }

    public boolean isTokenValid(final String token) {
        parseClaims(token);
        return true;
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
