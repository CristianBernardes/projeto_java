package br.com.projetojava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point.
 *
 * <p>REST API built with Spring Boot 3, JPA/Hibernate, PostgreSQL and JWT authentication.
 *
 * <p>{@link EnableScheduling} is required by the outbox event poller ({@code
 * OutboxEventPublisher}), which relays pending events to RabbitMQ/Kafka on a fixed delay.
 */
@SpringBootApplication
@EnableScheduling
public class ProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectApplication.class, args);
  }
}
