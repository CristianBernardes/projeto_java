package br.com.projetojava.order.domain;

import br.com.projetojava.product.domain.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a single line item within an {@link Order}.
 *
 * <p>Captures the product, quantity and unit price <em>at the time of purchase</em> — the unit
 * price is copied from the product so that later price changes do not affect historical orders.
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  /**
   * Creates a new order item, snapshotting the product's current price.
   *
   * @param product product being purchased
   * @param quantity quantity requested (must be positive)
   * @return new {@link OrderItem} instance (not yet attached to an order)
   * @throws IllegalArgumentException if the quantity is not positive
   */
  static OrderItem create(Product product, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Order item quantity must be positive.");
    }
    OrderItem item = new OrderItem();
    item.product = product;
    item.quantity = quantity;
    item.unitPrice = product.getPrice();
    return item;
  }

  /**
   * Associates this item with its parent order. Package-private: only {@link Order} may call this
   * when adding the item to its collection.
   *
   * @param order parent order
   */
  void attachTo(Order order) {
    this.order = order;
  }

  /**
   * Computes the subtotal for this line item ({@code unitPrice * quantity}).
   *
   * @return line item subtotal
   */
  public BigDecimal subtotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Two order items are equal if they share the same persisted {@code id}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrderItem other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type.
   *
   * @return constant hash for all {@link OrderItem} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
