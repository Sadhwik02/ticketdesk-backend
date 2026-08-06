package com.ticketdesk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration with JWT bearer security scheme.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Local Development")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .title("TicketDesk API")
                .description("""
                        ## TicketDesk — Enterprise IT Support Ticket Management System
                        
                        This API provides full lifecycle management for IT support tickets.
                        
                        ### Authentication
                        All endpoints (except `/auth/**`) require a valid **JWT Bearer token**.
                        Obtain a token via `POST /auth/login`, then click **Authorize** and enter:
                        ```
                        Bearer <your-access-token>
                        ```
                        
                        ### Roles
                        - **ADMIN** — Full system access
                        - **SUPPORT_ENGINEER** — Manage and resolve tickets
                        - **EMPLOYEE** — Create and track own tickets
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("TicketDesk Team")
                        .email("support@ticketdesk.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Provide your JWT access token. Example: Bearer eyJhbGci...");
    }
}
