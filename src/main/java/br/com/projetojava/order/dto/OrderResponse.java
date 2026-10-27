package br.com.projetojava.order.dto;

import br.com.projetojava.order.domain.Order;
import br.com.projetojava.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Output DTO representing an order in the API response.
 *
 * @param id unique identifier
 * @param userId identifier of the customer who placed the order
 * @param status current lifecycle status
 * @param totalAmount total order amount
 * @param items line items
 * @param failureReason explanation if the order failed, or {@code null}
 * @param createdAt creation date/time
 * @param updatedAt last update date/time
 */
@Schema(description = "Order representation")
public record OrderResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Customer identifier", example = "2") Long userId,
    @Schema(description = "Current order status") OrderStatus status,
    @Schema(description = "Total order amount", example = "599.80") BigDecimal totalAmount,
    @Schema(description = "Line items") List<OrderItemResponse> items,
    @Schema(description = "Failure reason, present only when status is FAILED")
        String failureReason,
    @Schema(description = "Creation date and time") LocalDateTime createdAt,
    @Schema(description = "Last update date and time") LocalDateTime updatedAt) {

  /**
   * Converts an {@link Order} entity to this response DTO.
   *
   * @param order entity to convert
   * @return DTO populated with entity data
   */
  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getUser().getId(),
        order.getStatus(),
        order.getTotalAmount(),
        order.getItems().stream().map(OrderItemResponse::from).toList(),
        order.getFailureReason(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }
}
