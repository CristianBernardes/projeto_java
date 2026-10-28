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
@Schema(description = "Um item dentro de uma resposta de pedido")
public record OrderItemResponse(
    @Schema(description = "Identificador do produto", example = "1") Long productId,
    @Schema(description = "Nome do produto", example = "Mechanical Keyboard") String productName,
    @Schema(description = "Quantidade comprada", example = "2") int quantity,
    @Schema(description = "Preço unitário no momento da compra", example = "299.90")
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
