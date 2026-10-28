package br.com.projetojava.order.messaging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.order.domain.DeliveryAddress;
import br.com.projetojava.order.domain.Order;
import br.com.projetojava.order.domain.OrderStatus;
import br.com.projetojava.order.event.OrderCreatedEvent;
import br.com.projetojava.order.event.OrderCreatedEvent.OrderItemPayload;
import br.com.projetojava.order.repository.OrderRepository;
import br.com.projetojava.outbox.service.OutboxEventServicePort;
import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReservationService")
class StockReservationServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private ProductRepository productRepository;
  @Mock private OutboxEventServicePort outboxEventService;

  @InjectMocks private StockReservationService stockReservationService;

  private static final User USER = User.create("customer@example.com", "hash", "USER");
  private static final DeliveryAddress ADDRESS =
      new DeliveryAddress("Rua A, 100", "São Paulo", "SP", "01000-000");

  @Test
  @DisplayName("reserveStock — should confirm order when every item has enough stock")
  void reserveStock_shouldConfirmOrderWhenStockAvailable() {
    Product keyboard = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);
    setProductId(keyboard, 1L);
    Order order = Order.create(USER, ADDRESS, List.of(new Order.OrderItemRequest(keyboard, 2)));
    setOrderId(order, 1L);

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            1L,
            1L,
            new BigDecimal("200.00"),
            List.of(new OrderItemPayload(1L, 2)),
            LocalDateTime.now());

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(keyboard));

    stockReservationService.reserveStock(event);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(keyboard.getStock()).isEqualTo(8);
    verify(outboxEventService).append(eq("Order"), eq(1L), eq("order.confirmed"), any());
  }

  @Test
  @DisplayName(
      "reserveStock — should fail order and compensate already-debited items when stock is insufficient")
  void reserveStock_shouldFailAndCompensateWhenStockInsufficient() {
    Product keyboard = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);
    setProductId(keyboard, 1L);
    Product mouse = Product.create("Mouse", null, new BigDecimal("50.00"), 1);
    setProductId(mouse, 2L);
    Order order =
        Order.create(
            USER,
            ADDRESS,
            List.of(new Order.OrderItemRequest(keyboard, 2), new Order.OrderItemRequest(mouse, 5)));
    setOrderId(order, 1L);

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            1L,
            1L,
            new BigDecimal("450.00"),
            List.of(new OrderItemPayload(1L, 2), new OrderItemPayload(2L, 5)),
            LocalDateTime.now());

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(keyboard));
    when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(mouse));

    stockReservationService.reserveStock(event);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    assertThat(order.getFailureReason()).contains("Estoque insuficiente");
    // Keyboard was debited (10 -> 8) then compensated back (8 -> 10): no net change.
    assertThat(keyboard.getStock()).isEqualTo(10);
    // Mouse never had enough stock to begin with (1 < 5): unchanged.
    assertThat(mouse.getStock()).isEqualTo(1);
    verify(outboxEventService).append(eq("Order"), eq(1L), eq("order.stock-failed"), any());
  }

  @Test
  @DisplayName("reserveStock — should be a no-op when order is no longer PENDING")
  void reserveStock_shouldSkipWhenOrderNotPending() {
    Product keyboard = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);
    Order order = Order.create(USER, ADDRESS, List.of(new Order.OrderItemRequest(keyboard, 2)));
    setOrderId(order, 1L);
    order.confirm();

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            1L,
            1L,
            new BigDecimal("200.00"),
            List.of(new OrderItemPayload(1L, 2)),
            LocalDateTime.now());

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    stockReservationService.reserveStock(event);

    verify(productRepository, never()).findByIdForUpdate(anyLong());
    verify(outboxEventService, never()).append(any(), any(), any(), any());
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private void setOrderId(Order order, Long id) {
    try {
      var field = Order.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(order, id);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private void setProductId(Product product, Long id) {
    try {
      var field = Product.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(product, id);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }
}
