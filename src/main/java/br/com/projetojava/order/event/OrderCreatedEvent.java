package br.com.projetojava.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain event published when a new order is created ({@code event_type = "order.created"}).
 *
 * <p>Serialized as the outbox event payload and consumed by:
 *
 * <ul>
 *   <li>the RabbitMQ stock reservation consumer, which debits stock for each item
 *   <li>the Kafka {@code order-events} topic, for auditing and independent consumers
 * </ul>
 *
 * @param orderId identifier of the created order
 * @param userId identifier of the customer who placed the order
 * @param totalAmount total order amount
 * @param items line items to reserve stock for
 * @param createdAt order creation timestamp
 */
public record OrderCreatedEvent(
    Long orderId,
    Long userId,
    BigDecimal totalAmount,
    List<OrderItemPayload> items,
    LocalDateTime createdAt) {

  /**
   * Line item payload within an {@link OrderCreatedEvent}.
   *
   * @param productId identifier of the product to reserve stock for
   * @param quantity quantity to reserve
   */
  public record OrderItemPayload(Long productId, int quantity) {}
}
