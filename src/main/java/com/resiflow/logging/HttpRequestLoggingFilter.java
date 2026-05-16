package com.resiflow.logging;

import com.resiflow.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveOrGenerate(request.getHeader(CORRELATION_ID_HEADER));
        String requestId = resolveOrGenerate(request.getHeader(REQUEST_ID_HEADER));
        long startedAt = System.nanoTime();

        MDC.put("correlationId", correlationId);
        MDC.put("requestId", requestId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("requestPath", request.getRequestURI());
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        LOGGER.info(
                "HTTP request started method={} path={} remoteIp={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            int status = response.getStatus();
            AuthenticatedUser authenticatedUser = extractAuthenticatedUser();
            String userId = authenticatedUser == null || authenticatedUser.userId() == null
                    ? "anonymous"
                    : authenticatedUser.userId().toString();
            String residenceId = authenticatedUser == null || authenticatedUser.residenceId() == null
                    ? "n/a"
                    : authenticatedUser.residenceId().toString();

            if (status >= 500) {
                LOGGER.error(
                        "HTTP request completed with server error method={} path={} status={} durationMs={} userId={} residenceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        durationMs,
                        userId,
                        residenceId
                );
            } else if (status >= 400) {
                LOGGER.warn(
                        "HTTP request completed with client error method={} path={} status={} durationMs={} userId={} residenceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        durationMs,
                        userId,
                        residenceId
                );
            } else {
                LOGGER.info(
                        "HTTP request completed method={} path={} status={} durationMs={} userId={} residenceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        durationMs,
                        userId,
                        residenceId
                );
            }
            MDC.clear();
        }
    }

    private String resolveOrGenerate(final String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return headerValue.trim();
    }

    private AuthenticatedUser extractAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal;
    }
}
