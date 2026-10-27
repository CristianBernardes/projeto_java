package br.com.projetojava.order.dto;

import br.com.projetojava.order.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Output DTO representing a single line item within an order response.
 *
 * @param productId identifier of the purchased product
 * @param productName name of the purchased product
 * @param quantity quantity purchased
 * @param unitPrice unit price at the time of purchase
 * @param subtotal {@code unitPrice * quantity}
 */
@Schema(description = "A line item within an order response")
public record OrderItemResponse(
    @Schema(description = "Product identifier", example = "1") Long productId,
    @Schema(description = "Product name", example = "Mechanical Keyboard") String productName,
    @Schema(description = "Quantity purchased", example = "2") int quantity,
    @Schema(description = "Unit price at the time of purchase", example = "299.90")
        BigDecimal unitPrice,
    @Schema(description = "Subtotal (unitPrice * quantity)", example = "599.80")
        BigDecimal subtotal) {

  /**
   * Converts an {@link OrderItem} entity to this response DTO.
   *
   * @param item entity to convert
   * @return DTO populated with entity data
   */
  public static OrderItemResponse from(OrderItem item) {
    return new OrderItemResponse(
        item.getProduct().getId(),
        item.getProduct().getName(),
        item.getQuantity(),
        item.getUnitPrice(),
        item.subtotal());
  }
}
