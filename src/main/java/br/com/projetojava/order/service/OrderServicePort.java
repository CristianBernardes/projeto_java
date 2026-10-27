package br.com.projetojava.order.service;

import br.com.projetojava.order.dto.CreateOrderRequest;
import br.com.projetojava.order.dto.OrderResponse;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;

/**
 * Contract for the order domain use cases.
 *
 * <p>Consumers depend on this interface, never on the concrete implementation, respecting the
 * <em>Dependency Inversion Principle</em>.
 */
public interface OrderServicePort {

  /**
   * Creates a new order in {@code PENDING} status and schedules asynchronous stock reservation.
   *
   * <p>Stock is <strong>not</strong> debited synchronously. The order is persisted together with an
   * outbox event (same transaction, Transactional Outbox pattern); a separate poller then publishes
   * that event so a message consumer can reserve stock in the background.
   *
   * @param request order creation data
   * @param userId identifier of the customer placing the order
   * @return response DTO of the created order, in {@code PENDING} status
   * @throws ResourceNotFoundException if the user or any referenced product does not exist
   */
  OrderResponse create(CreateOrderRequest request, Long userId);

  /**
   * Finds an order by its identifier.
   *
   * @param id order identifier
   * @return response DTO of the found order
   * @throws ResourceNotFoundException if the order does not exist
   */
  OrderResponse findById(Long id);
}
