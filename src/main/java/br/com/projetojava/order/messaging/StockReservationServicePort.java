package br.com.projetojava.order.messaging;

import br.com.projetojava.order.event.OrderCreatedEvent;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;

/**
 * Contract for the order processing saga's stock reservation step.
 *
 * <p>Consumers ({@link StockReservationConsumer}) depend on this interface, never on the concrete
 * implementation, respecting the <em>Dependency Inversion Principle</em>.
 */
public interface StockReservationServicePort {

  /**
   * Reserves stock for every item of the given order, confirming or failing the order accordingly.
   *
   * @param event the {@code order.created} event describing items to reserve
   * @throws ResourceNotFoundException if the order no longer exists
   */
  void reserveStock(OrderCreatedEvent event);
}
