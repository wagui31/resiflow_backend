package com.resiflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(final JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!hasBearerToken(authorizationHeader)
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "Skipping JWT authentication for path={} hasBearerToken={} alreadyAuthenticated={}",
                        request.getRequestURI(),
                        hasBearerToken(authorizationHeader),
                        SecurityContextHolder.getContext().getAuthentication() != null
                );
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            if (jwtService.isTokenValid(token)) {
                AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                        jwtService.extractUserId(token),
                        jwtService.extractSubject(token),
                        jwtService.extractResidenceId(token),
                        jwtService.extractRole(token)
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authenticatedUser.authorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                LOGGER.debug(
                        "JWT authentication established for userId={} residenceId={} role={} path={}",
                        authenticatedUser.userId(),
                        authenticatedUser.residenceId(),
                        authenticatedUser.role(),
                        request.getRequestURI()
                );
            }
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            LOGGER.warn("Invalid JWT token for path={}: {}", request.getRequestURI(), exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasBearerToken(final String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX);
    }
}
