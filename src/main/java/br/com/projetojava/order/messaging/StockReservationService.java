package br.com.projetojava.order.messaging;

import br.com.projetojava.order.domain.Order;
import br.com.projetojava.order.domain.OrderStatus;
import br.com.projetojava.order.event.OrderConfirmedEvent;
import br.com.projetojava.order.event.OrderCreatedEvent;
import br.com.projetojava.order.event.OrderCreatedEvent.OrderItemPayload;
import br.com.projetojava.order.event.OrderStockFailedEvent;
import br.com.projetojava.order.repository.OrderRepository;
import br.com.projetojava.outbox.service.OutboxEventServicePort;
import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.InsufficientStockException;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core business logic of the order processing saga's stock reservation step.
 *
 * <p>Separated from {@link StockReservationConsumer} (the RabbitMQ adapter) so the saga logic is
 * unit-testable without a running broker.
 *
 * <p><strong>Saga with compensation:</strong> items are debited one at a time using a pessimistic
 * row lock ({@link ProductRepository#findByIdForUpdate}). If any item has insufficient stock, every
 * item already debited <em>for this same order</em> is credited back (compensating action) before
 * the order is marked {@link br.com.projetojava.order.domain.OrderStatus#FAILED} — the order never
 * ends up in a state where only some of its items reserved stock.
 *
 * <p>Implements {@link StockReservationServicePort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationService implements StockReservationServicePort {

  private static final String AGGREGATE_TYPE = "Order";

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OutboxEventServicePort outboxEventService;

  /**
   * Reserves stock for every item of the given order, confirming or failing the order accordingly.
   *
   * <p>Runs in a single transaction: the stock debits (or compensating credits), the order status
   * update, and the resulting {@code order.confirmed}/{@code order.stock-failed} outbox event are
   * all committed — or rolled back — together.
   *
   * @param event the {@code order.created} event describing items to reserve
   * @throws ResourceNotFoundException if the order no longer exists
   */
  @Override
  @Transactional
  public void reserveStock(OrderCreatedEvent event) {
    Order order =
        orderRepository
            .findById(event.orderId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found. id=" + event.orderId()));

    if (order.getStatus() != OrderStatus.PENDING) {
      log.info(
          "Order id={} is no longer PENDING (status={}); skipping stock reservation.",
          order.getId(),
          order.getStatus());
      return;
    }

    List<Product> debited = new ArrayList<>();

    try {
      for (OrderItemPayload item : event.items()) {
        debitStock(item, debited);
      }
      order.confirm();
      orderRepository.save(order);
      outboxEventService.append(
          AGGREGATE_TYPE, order.getId(), "order.confirmed", new OrderConfirmedEvent(order.getId()));
      log.info("Stock reserved and order confirmed. order_id={}.", order.getId());

    } catch (InsufficientStockException e) {
      compensate(debited, event.items());
      order.fail(e.getMessage());
      orderRepository.save(order);
      outboxEventService.append(
          AGGREGATE_TYPE,
          order.getId(),
          "order.stock-failed",
          new OrderStockFailedEvent(order.getId(), e.getMessage()));
      log.warn(
          "Stock reservation failed; order compensated. order_id={}, reason='{}'.",
          order.getId(),
          e.getMessage());
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void debitStock(OrderItemPayload item, List<Product> debited) {
    Product product =
        productRepository
            .findByIdForUpdate(item.productId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found. id=" + item.productId()));

    if (product.getStock() < item.quantity()) {
      throw new InsufficientStockException(
          "Insufficient stock for product id=%d. Available: %d, requested: %d."
              .formatted(product.getId(), product.getStock(), item.quantity()));
    }

    product.removeStock(item.quantity());
    productRepository.save(product);
    debited.add(product);
  }

  /**
   * Credits back the stock of every item successfully debited before the failure, using the
   * original requested quantities from the event (the {@code debited} list tracks which products
   * need compensation; the quantities are read back from the event by product id).
   *
   * @param debited products whose stock was already decremented for this order
   * @param requestedItems original item payloads, used to look up each product's requested quantity
   */
  private void compensate(List<Product> debited, List<OrderItemPayload> requestedItems) {
    for (Product product : debited) {
      int quantity =
          requestedItems.stream()
              .filter(item -> item.productId().equals(product.getId()))
              .findFirst()
              .map(OrderItemPayload::quantity)
              .orElse(0);
      if (quantity > 0) {
        product.addStock(quantity);
        productRepository.save(product);
        log.info("Compensated stock for product id={}, quantity={}.", product.getId(), quantity);
      }
    }
  }
}
