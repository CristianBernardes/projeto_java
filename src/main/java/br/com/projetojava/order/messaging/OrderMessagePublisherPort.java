package br.com.projetojava.order.messaging;

/**
 * Contract for publishing order processing messages to the RabbitMQ work queue.
 *
 * <p>Consumers ({@link br.com.projetojava.outbox.service.SingleOutboxEventPublisher}) depend on
 * this interface, never on the concrete implementation, respecting the <em>Dependency Inversion
 * Principle</em>.
 */
public interface OrderMessagePublisherPort {

  /**
   * Publishes an {@code order.created} message to the RabbitMQ work queue.
   *
   * @param messageId unique identifier for this message (used by the consumer's idempotency guard
   *     to detect redeliveries)
   * @param payload event payload, serialized to JSON by the configured message converter
   */
  void publishOrderCreated(String messageId, Object payload);
}
