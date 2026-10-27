package br.com.projetojava.order.event;

/**
 * Domain event published when stock reservation fails for an order ({@code event_type =
 * "order.stock-failed"}).
 *
 * <p>This is the compensating event of the order processing saga: when the stock reservation
 * consumer finds insufficient stock, it publishes this event instead of silently failing. A
 * dedicated listener then marks the order as {@link
 * br.com.projetojava.order.domain.OrderStatus#FAILED} and releases any stock already debited for
 * earlier items in the same order (compensation).
 *
 * @param orderId identifier of the order whose stock reservation failed
 * @param reason human-readable explanation of the failure
 */
public record OrderStockFailedEvent(Long orderId, String reason) {}
