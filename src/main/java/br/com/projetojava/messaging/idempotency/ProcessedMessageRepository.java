package br.com.projetojava.messaging.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for {@link ProcessedMessage}. */
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {}
