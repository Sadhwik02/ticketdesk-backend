package com.ticketdesk;

import com.ticketdesk.config.FileStorageConfig;
import com.ticketdesk.config.OpenApiConfig;
import com.ticketdesk.config.RequestLoggingFilter;
import com.ticketdesk.config.SecurityConfig;
import com.ticketdesk.security.CustomUserDetailsService;
import com.ticketdesk.security.JwtAuthEntryPoint;
import com.ticketdesk.security.JwtAuthenticationFilter;
import com.ticketdesk.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Shared test configuration providing mocks for Spring Security infrastructure beans
 * required by {@code @WebMvcTest} slices.
 */
@TestConfiguration
public class TestSecurityConfig {
    // All these beans are needed for the security filter chain in WebMvcTest context
    @MockBean JwtTokenProvider        jwtTokenProvider;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean JwtAuthEntryPoint       jwtAuthEntryPoint;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean FileStorageConfig       fileStorageConfig;
}
