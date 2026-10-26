package br.com.projetojava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação.
 *
 * <p>API REST de estudo construída com Spring Boot 3, JPA/Hibernate e PostgreSQL.
 */
@SpringBootApplication
public class ProjetoJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjetoJavaApplication.class, args);
  }
}
