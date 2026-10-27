package br.com.projetojava.product.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain entity representing a product in the catalog.
 *
 * <p>Follows the rich entity pattern: business rules concerning the product state live here, not in
 * services.
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  @Setter
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  @Setter
  private String description;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "stock", nullable = false)
  private int stock;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Creates a new product with the provided data.
   *
   * @param name product name
   * @param description optional description
   * @param price unit price (must be positive)
   * @param stock initial stock quantity (must be non-negative)
   * @return new {@link Product} instance
   * @throws IllegalArgumentException if price or stock are invalid
   */
  public static Product create(String name, String description, BigDecimal price, int stock) {
    validatePrice(price);
    validateStock(stock);

    Product product = new Product();
    product.name = name;
    product.description = description;
    product.price = price;
    product.stock = stock;
    product.active = true;
    product.createdAt = LocalDateTime.now();
    product.updatedAt = LocalDateTime.now();
    return product;
  }

  /**
   * Updates the product price.
   *
   * @param newPrice new price (must be positive)
   * @throws IllegalArgumentException if the price is invalid
   */
  public void updatePrice(BigDecimal newPrice) {
    validatePrice(newPrice);
    this.price = newPrice;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Updates the stock to an absolute quantity.
   *
   * @param newStock new stock quantity (must be non-negative)
   * @throws IllegalArgumentException if the quantity is invalid
   */
  public void updateStock(int newStock) {
    validateStock(newStock);
    this.stock = newStock;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Adds units to the stock.
   *
   * @param quantity quantity to add (must be positive)
   * @throws IllegalArgumentException if the quantity is invalid
   */
  public void addStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity to add must be positive.");
    }
    this.stock += quantity;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Removes units from the stock.
   *
   * @param quantity quantity to remove (must be positive and not exceed current stock)
   * @throws IllegalArgumentException if the quantity is invalid or insufficient
   */
  public void removeStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity to remove must be positive.");
    }
    if (quantity > this.stock) {
      throw new IllegalArgumentException(
          "Insufficient stock. Available: %d, requested: %d.".formatted(this.stock, quantity));
    }
    this.stock -= quantity;
    this.updatedAt = LocalDateTime.now();
  }

  /** Deactivates the product, making it unavailable in the catalog. */
  public void deactivate() {
    this.active = false;
    this.updatedAt = LocalDateTime.now();
  }

  /** Reactivates the product. */
  public void activate() {
    this.active = true;
    this.updatedAt = LocalDateTime.now();
  }

  // ── Private validations ──────────────────────────────────────────────────────

  private static void validatePrice(BigDecimal price) {
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive.");
    }
  }

  private static void validateStock(int stock) {
    if (stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative.");
    }
  }

  // ── equals / hashCode based on business key ──────────────────────────────────

  /**
   * Two products are equal if they share the same persisted {@code id}.
   *
   * <p>Transient products (null id) are never equal to each other, avoiding issues in collections
   * before {@code persist}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Product other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type, ensuring stability in {@code Set} and {@code Map} even before
   * {@code persist} (when id is still {@code null}).
   *
   * @return constant hash for all {@link Product} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
