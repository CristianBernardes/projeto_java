package br.com.projetojava.order.domain;

import static org.assertj.core.api.Assertions.*;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.product.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order")
class OrderTest {

  private static final User USER = User.create("customer@example.com", "hash", "USER");
  private static final DeliveryAddress ADDRESS =
      new DeliveryAddress("Rua A, 100", "São Paulo", "SP", "01000-000");

  @Test
  @DisplayName("create — should compute total amount from item subtotals")
  void create_shouldComputeTotalAmount() {
    Product keyboard = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);
    Product mouse = Product.create("Mouse", null, new BigDecimal("50.00"), 10);

    Order order =
        Order.create(
            USER,
            ADDRESS,
            List.of(new Order.OrderItemRequest(keyboard, 2), new Order.OrderItemRequest(mouse, 1)));

    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.getTotalAmount()).isEqualByComparingTo("250.00");
    assertThat(order.getItems()).hasSize(2);
  }

  @Test
  @DisplayName("create — should throw IllegalArgumentException when no items are given")
  void create_shouldThrowWhenNoItems() {
    assertThatThrownBy(() -> Order.create(USER, ADDRESS, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least one item");
  }

  @Test
  @DisplayName("confirm — should transition PENDING order to CONFIRMED")
  void confirm_shouldTransitionToConfirmed() {
    Order order = createPendingOrder();

    order.confirm();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  @DisplayName("confirm — should throw IllegalStateException when order is not PENDING")
  void confirm_shouldThrowWhenNotPending() {
    Order order = createPendingOrder();
    order.confirm();

    assertThatThrownBy(order::confirm).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("fail — should transition PENDING order to FAILED with a reason")
  void fail_shouldTransitionToFailedWithReason() {
    Order order = createPendingOrder();

    order.fail("Insufficient stock.");

    assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    assertThat(order.getFailureReason()).isEqualTo("Insufficient stock.");
  }

  @Test
  @DisplayName("cancel — should transition PENDING order to CANCELLED")
  void cancel_shouldTransitionToCancelled() {
    Order order = createPendingOrder();

    order.cancel();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private Order createPendingOrder() {
    Product product = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);
    return Order.create(USER, ADDRESS, List.of(new Order.OrderItemRequest(product, 1)));
  }
}
