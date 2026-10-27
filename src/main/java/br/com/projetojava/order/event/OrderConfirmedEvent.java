package br.com.projetojava.order.event;

/**
 * Domain event published when stock has been successfully reserved for every item in an order
 * ({@code event_type = "order.confirmed"}).
 *
 * <p>Consumed by a listener that marks the order as {@link
 * br.com.projetojava.order.domain.OrderStatus#CONFIRMED} and is also forwarded to Kafka for
 * auditing.
 *
 * @param orderId identifier of the confirmed order
 */
public record OrderConfirmedEvent(Long orderId) {}
