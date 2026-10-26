package br.com.projetojava.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 documentation configuration (Swagger UI).
 *
 * <p>Available at:
 *
 * <ul>
 *   <li>Interactive UI: <a href="http://localhost:8088/swagger-ui.html">/swagger-ui.html</a>
 *   <li>JSON spec: <a href="http://localhost:8088/api-docs">/api-docs</a>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

  /**
   * Defines the global API metadata exposed in Swagger UI.
   *
   * @return configured {@link OpenAPI} instance
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Project Java — REST API")
                .description(
                    """
                    REST API built with **Spring Boot 3**, **JPA/Hibernate**, **PostgreSQL** and **JWT authentication**.

                    Covers the **Products** domain: creation, retrieval, update and deactivation (soft delete).

                    Includes **JWT authentication**, **file upload**, and a full **Chain of Responsibility** exception handler.
                    """)
                .version("v1.0.0")
                .contact(new Contact().name("Cristian Bernarde"))
                .license(new License().name("MIT")))
        .servers(List.of(new Server().url("http://localhost:8088").description("Local (Docker)")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtained from POST /api/v1/auth/login")));
  }
}
