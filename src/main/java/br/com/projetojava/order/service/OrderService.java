package br.com.projetojava.order.service;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.order.domain.DeliveryAddress;
import br.com.projetojava.order.domain.Order;
import br.com.projetojava.order.dto.CreateOrderRequest;
import br.com.projetojava.order.dto.OrderItemRequestDto;
import br.com.projetojava.order.dto.OrderResponse;
import br.com.projetojava.order.event.OrderCreatedEvent;
import br.com.projetojava.order.event.OrderCreatedEvent.OrderItemPayload;
import br.com.projetojava.order.repository.OrderRepository;
import br.com.projetojava.outbox.service.OutboxEventServicePort;
import br.com.projetojava.product.domain.Product;
import br.com.projetojava.product.repository.ProductRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the order domain use cases.
 *
 * <p>Implements {@link OrderServicePort} to respect the <em>Dependency Inversion Principle</em>:
 * consumers depend on the interface, not on this concrete class.
 *
 * <p>Follows the <strong>Transactional Outbox</strong> pattern: persisting the order and appending
 * the {@code order.created} outbox event happen in the same {@code @Transactional} boundary, so the
 * order is never saved without its corresponding event being recorded (and eventually published).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderServicePort {

  private static final String AGGREGATE_TYPE = "Order";
  private static final String EVENT_TYPE_CREATED = "order.created";

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final OutboxEventServicePort outboxEventService;

  /**
   * Creates a new order and appends the {@code order.created} outbox event in the same transaction.
   *
   * @param request order creation data
   * @param userId identifier of the customer placing the order
   * @return response DTO of the created order, in {@code PENDING} status
   * @throws ResourceNotFoundException if the user or any referenced product does not exist
   */
  @Override
  @Transactional
  public OrderResponse create(CreateOrderRequest request, Long userId) {
    log.info("Creating order for user_id={}.", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found. id=" + userId));

    List<Order.OrderItemRequest> itemRequests = resolveItems(request.items());
    DeliveryAddress deliveryAddress = toDeliveryAddress(request);

    Order order = Order.create(user, deliveryAddress, itemRequests);
    Order saved = orderRepository.save(order);

    outboxEventService.append(AGGREGATE_TYPE, saved.getId(), EVENT_TYPE_CREATED, toEvent(saved));

    log.info("Order created id={}, status={}.", saved.getId(), saved.getStatus());
    return OrderResponse.from(saved);
  }

  /**
   * Finds an order by its identifier.
   *
   * @param id order identifier
   * @return response DTO of the found order
   * @throws ResourceNotFoundException if the order does not exist
   */
  @Override
  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    log.info("Finding order id={}.", id);
    return OrderResponse.from(findEntity(id));
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private List<Order.OrderItemRequest> resolveItems(List<OrderItemRequestDto> itemDtos) {
    return itemDtos.stream().map(this::resolveItem).toList();
  }

  private Order.OrderItemRequest resolveItem(OrderItemRequestDto dto) {
    Product product =
        productRepository
            .findById(dto.productId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found. id=" + dto.productId()));
    return new Order.OrderItemRequest(product, dto.quantity());
  }

  private DeliveryAddress toDeliveryAddress(CreateOrderRequest request) {
    return new DeliveryAddress(
        request.deliveryAddress().street(),
        request.deliveryAddress().city(),
        request.deliveryAddress().state(),
        request.deliveryAddress().zip());
  }

  private OrderCreatedEvent toEvent(Order order) {
    List<OrderItemPayload> items =
        order.getItems().stream()
            .map(item -> new OrderItemPayload(item.getProduct().getId(), item.getQuantity()))
            .toList();
    return new OrderCreatedEvent(
        order.getId(),
        order.getUser().getId(),
        order.getTotalAmount(),
        items,
        order.getCreatedAt());
  }

  private Order findEntity(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found. id=" + id));
  }
}
