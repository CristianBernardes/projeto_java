package br.com.projetojava.order.messaging;

/**
 * Contract for publishing order domain events to the Kafka event stream.
 *
 * <p>Consumers ({@link br.com.projetojava.outbox.service.SingleOutboxEventPublisher}) depend on
 * this interface, never on the concrete implementation, respecting the <em>Dependency Inversion
 * Principle</em>.
 */
public interface OrderEventPublisherPort {

  /**
   * Publishes an order event to the {@code order-events} topic, keyed by order id.
   *
   * @param orderId identifier of the order the event refers to (used as the partition key)
   * @param eventType event type/name (e.g., {@code "order.created"})
   * @param payload event payload, serialized to JSON by the configured Kafka producer
   */
  void publish(Long orderId, String eventType, Object payload);
}
