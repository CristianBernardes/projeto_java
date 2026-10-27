package br.com.projetojava.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value object representing the delivery address of an {@link Order}.
 *
 * <p>Embedded directly into the {@code orders} table (no separate table) since the address has no
 * identity or lifecycle of its own outside the order.
 *
 * @param street street name and number
 * @param city delivery city
 * @param state two-letter state code (e.g., "SP", "RJ")
 * @param zip postal code
 */
@Embeddable
public record DeliveryAddress(
    @Column(name = "delivery_street", nullable = false) String street,
    @Column(name = "delivery_city", nullable = false) String city,
    @Column(name = "delivery_state", nullable = false, length = 2) String state,
    @Column(name = "delivery_zip", nullable = false) String zip) {

  /**
   * Validates the address fields.
   *
   * @throws IllegalArgumentException if any required field is blank
   */
  public DeliveryAddress {
    if (street == null || street.isBlank()) {
      throw new IllegalArgumentException("Delivery street must not be blank.");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("Delivery city must not be blank.");
    }
    if (state == null || state.isBlank()) {
      throw new IllegalArgumentException("Delivery state must not be blank.");
    }
    if (zip == null || zip.isBlank()) {
      throw new IllegalArgumentException("Delivery zip code must not be blank.");
    }
  }
}
