package br.com.projetojava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * <p>REST API built with Spring Boot 3, JPA/Hibernate, PostgreSQL and JWT authentication.
 */
@SpringBootApplication
public class ProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectApplication.class, args);
  }
}
