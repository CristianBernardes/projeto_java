package br.com.projetojava.order.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.order.domain.Order;
import br.com.projetojava.order.dto.CreateOrderRequest;
import br.com.projetojava.order.dto.DeliveryAddressRequest;
import br.com.projetojava.order.dto.OrderItemRequestDto;
import br.com.projetojava.order.dto.OrderResponse;
import br.com.projetojava.order.repository.OrderRepository;
import br.com.projetojava.outbox.service.OutboxEventServicePort;
import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private OutboxEventServicePort outboxEventService;

  @InjectMocks private OrderService orderService;

  @Test
  @DisplayName("create — should persist order in PENDING status and append outbox event")
  void create_shouldPersistOrderAndAppendOutboxEvent() {
    User user = User.create("customer@example.com", "hash", "USER");
    Product product = Product.create("Keyboard", null, new BigDecimal("100.00"), 10);

    CreateOrderRequest request =
        new CreateOrderRequest(
            new DeliveryAddressRequest("Rua A, 100", "São Paulo", "SP", "01000-000"),
            List.of(new OrderItemRequestDto(1L, 2)));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            inv -> {
              Order order = inv.getArgument(0);
              setOrderId(order, 10L);
              return order;
            });

    OrderResponse response = orderService.create(request, 1L);

    assertThat(response.status().name()).isEqualTo("PENDING");
    assertThat(response.totalAmount()).isEqualByComparingTo("200.00");
    verify(orderRepository).save(any(Order.class));
    verify(outboxEventService).append(eq("Order"), eq(10L), eq("order.created"), any());
  }

  @Test
  @DisplayName("create — should throw ResourceNotFoundException when user does not exist")
  void create_shouldThrowWhenUserNotFound() {
    CreateOrderRequest request =
        new CreateOrderRequest(
            new DeliveryAddressRequest("Rua A, 100", "São Paulo", "SP", "01000-000"),
            List.of(new OrderItemRequestDto(1L, 2)));

    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(request, 99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  @DisplayName("create — should throw ResourceNotFoundException when a product does not exist")
  void create_shouldThrowWhenProductNotFound() {
    User user = User.create("customer@example.com", "hash", "USER");
    CreateOrderRequest request =
        new CreateOrderRequest(
            new DeliveryAddressRequest("Rua A, 100", "São Paulo", "SP", "01000-000"),
            List.of(new OrderItemRequestDto(99L, 2)));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(request, 1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  @DisplayName("findById — should throw ResourceNotFoundException when order does not exist")
  void findById_shouldThrowWhenNotFound() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("99");
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
}
