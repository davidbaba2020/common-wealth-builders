package com.common_wealth_builders.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${application.version:1.0.0}")
    private String appVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Commonwealth Builders API")
                        .version(appVersion)
                        .description("""
                                # Commonwealth Builders Management System API
                                
                                A comprehensive API for managing wealth building operations including:
                                - User Management & Authentication
                                - Payment Processing & Verification
                                - Expense Management & Approval
                                - Financial Reporting (PDF, Email, WhatsApp)
                                - Role-based Access Control
                                - Audit Trail Tracking
                                
                                ## Authentication
                                This API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:
```
                                Authorization: Bearer <your-token>
```
                                
                                ## Roles & Permissions
                                - **SUPER_ADMIN**: Full system access
                                - **TECH_ADMIN**: Technical administration
                                - **FIN_ADMIN**: Financial administration
                                - **USER**: Standard user access
                                
                                ## Response Format
                                All endpoints return a standardized response format:
```json
                                {
                                  "success": true,
                                  "message": "Operation successful",
                                  "data": { ... },
                                  "timestamp": "2026-02-05T10:30:00Z"
                                }
```
                                """)
                        .contact(new Contact()
                                .name("Commonwealth Builders Support")
                                .email("support@commonwealthbuilders.com")
                                .url("https://commonwealthbuilders.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://commonwealthbuilders.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort +"/api")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.commonwealthbuilders.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /v1/auth/login endpoint")));
    }
}