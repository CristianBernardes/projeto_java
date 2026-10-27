package br.com.projetojava.outbox.service;

import br.com.projetojava.outbox.domain.OutboxEvent;

/**
 * Contract for publishing a single outbox event to the configured message brokers.
 *
 * <p>Consumers ({@link OutboxEventPublisher}) depend on this interface, never on the concrete
 * implementation, respecting the <em>Dependency Inversion Principle</em>.
 */
public interface SingleOutboxEventPublisherPort {

  /**
   * Publishes a single outbox event in its own transaction, so one failure does not roll back or
   * block the rest of the batch being processed by the poller.
   *
   * @param event outbox event to publish
   */
  void publish(OutboxEvent event);
}
