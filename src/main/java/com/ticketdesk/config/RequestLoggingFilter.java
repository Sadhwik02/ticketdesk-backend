package com.ticketdesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs every HTTP request: method, URI, status, and execution time.
 * Skips actuator and static resource paths to reduce noise.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest  request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain        chain)
            throws ServletException, IOException {

        final long   startTime = System.currentTimeMillis();
        final String method    = request.getMethod();
        final String uri       = request.getRequestURI();

        log.info(">>> {} {}", method, uri);

        try {
            chain.doFilter(request, response);
        } finally {
            final long elapsed = System.currentTimeMillis() - startTime;
            log.info("<<< {} {} | Status: {} | Duration: {}ms",
                    method, uri, response.getStatus(), elapsed);
        }
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.contains("/swagger") || uri.contains("/v3/api-docs");
    }
}
