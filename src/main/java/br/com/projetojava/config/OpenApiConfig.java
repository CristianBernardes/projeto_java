package br.com.projetojava.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI 3 (Swagger UI).
 *
 * <p>Disponível em:
 *
 * <ul>
 *   <li>UI interativa: <a href="http://localhost:8088/swagger-ui.html">/swagger-ui.html</a>
 *   <li>JSON spec: <a href="http://localhost:8088/api-docs">/api-docs</a>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

  /**
   * Define os metadados globais da API expostos no Swagger UI.
   *
   * @return instância configurada de {@link OpenAPI}
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Projeto Java — API REST")
                .description(
                    """
                    API REST de estudo construída com **Spring Boot 3**, **JPA/Hibernate** e **PostgreSQL**.

                    Cobre o domínio de **Produtos**: criação, consulta, atualização e desativação (soft delete).

                    Desenvolvida como projeto de estudo para a vaga de Java/Grails.
                    """)
                .version("v1.0.0")
                .contact(new Contact().name("Cristian Bernarde"))
                .license(new License().name("MIT")))
        .servers(
            List.of(
                new Server().url("http://localhost:8088").description("Local (Docker)")));
  }
}
