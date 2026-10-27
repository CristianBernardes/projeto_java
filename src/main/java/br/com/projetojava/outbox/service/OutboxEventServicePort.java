package br.com.projetojava.outbox.service;

/**
 * Contract for appending domain events via the Transactional Outbox pattern.
 *
 * <p>Consumers ({@link br.com.projetojava.order.service.OrderService}, {@link
 * br.com.projetojava.order.messaging.StockReservationService}) depend on this interface, never on
 * the concrete implementation, respecting the <em>Dependency Inversion Principle</em>.
 */
public interface OutboxEventServicePort {

  /**
   * Serializes the given payload as JSON and appends it as a new unpublished outbox event.
   *
   * <p>Must be called from within the same transaction as the business change the event represents,
   * so both writes commit or roll back together.
   *
   * @param aggregateType name of the aggregate that produced the event (e.g., {@code "Order"})
   * @param aggregateId identifier of the aggregate instance
   * @param eventType event type/name (e.g., {@code "order.created"})
   * @param payload event payload object, serialized to JSON for storage
   * @throws IllegalStateException if the payload cannot be serialized
   */
  void append(String aggregateType, Long aggregateId, String eventType, Object payload);
}
