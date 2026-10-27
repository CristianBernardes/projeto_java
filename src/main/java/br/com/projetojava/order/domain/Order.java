package br.com.projetojava.order.domain;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.product.domain.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Aggregate root representing a customer order.
 *
 * <p>Follows the rich entity pattern: creation, item management and status transitions are all
 * business rules that live here, not in the service layer. Stock is intentionally
 * <strong>not</strong> debited when the order is created — reservation happens asynchronously (see
 * the {@code order} messaging package), which is why the order starts in {@link
 * OrderStatus#PENDING}.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OrderStatus status;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Embedded private DeliveryAddress deliveryAddress;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Creates a new order in {@link OrderStatus#PENDING}, with no stock debited yet.
   *
   * <p>At least one item is required. The {@code totalAmount} is computed from the item subtotals,
   * snapshotting each product's current price.
   *
   * @param user customer placing the order
   * @param deliveryAddress delivery address for this order
   * @param itemRequests list of (product, quantity) pairs to purchase
   * @return new {@link Order} instance in {@code PENDING} status
   * @throws IllegalArgumentException if no items are provided
   */
  public static Order create(
      User user, DeliveryAddress deliveryAddress, List<OrderItemRequest> itemRequests) {
    if (itemRequests == null || itemRequests.isEmpty()) {
      throw new IllegalArgumentException("Order must contain at least one item.");
    }

    Order order = new Order();
    order.user = user;
    order.deliveryAddress = deliveryAddress;
    order.status = OrderStatus.PENDING;
    order.createdAt = LocalDateTime.now();
    order.updatedAt = LocalDateTime.now();

    for (OrderItemRequest itemRequest : itemRequests) {
      order.addItem(itemRequest.product(), itemRequest.quantity());
    }
    order.totalAmount = order.computeTotal();

    return order;
  }

  /**
   * Returns an unmodifiable view of this order's items.
   *
   * @return read-only list of order items
   */
  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  /**
   * Confirms the order after stock has been successfully reserved for every item.
   *
   * @throws IllegalStateException if the order is not {@link OrderStatus#PENDING}
   */
  public void confirm() {
    requireStatus(OrderStatus.PENDING, "confirm");
    this.status = OrderStatus.CONFIRMED;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Marks the order as failed due to a compensating action (e.g., insufficient stock).
   *
   * @param reason human-readable explanation of the failure
   * @throws IllegalStateException if the order is not {@link OrderStatus#PENDING}
   */
  public void fail(String reason) {
    requireStatus(OrderStatus.PENDING, "fail");
    this.status = OrderStatus.FAILED;
    this.failureReason = reason;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Cancels the order before it has been confirmed.
   *
   * @throws IllegalStateException if the order is not {@link OrderStatus#PENDING}
   */
  public void cancel() {
    requireStatus(OrderStatus.PENDING, "cancel");
    this.status = OrderStatus.CANCELLED;
    this.updatedAt = LocalDateTime.now();
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void addItem(Product product, int quantity) {
    OrderItem item = OrderItem.create(product, quantity);
    item.attachTo(this);
    this.items.add(item);
  }

  private BigDecimal computeTotal() {
    return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void requireStatus(OrderStatus required, String action) {
    if (this.status != required) {
      throw new IllegalStateException(
          "Cannot %s order id=%d: expected status %s but was %s."
              .formatted(action, id, required, status));
    }
  }

  /**
   * Input pair (product, quantity) used to build an {@link Order} via {@link #create}.
   *
   * @param product product being purchased
   * @param quantity quantity requested (must be positive)
   */
  public record OrderItemRequest(Product product, int quantity) {}

  // ── equals / hashCode based on business key ──────────────────────────────────

  /**
   * Two orders are equal if they share the same persisted {@code id}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Order other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type.
   *
   * @return constant hash for all {@link Order} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
