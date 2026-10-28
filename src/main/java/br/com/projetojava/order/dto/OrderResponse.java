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
@Schema(description = "Representação de um pedido")
public record OrderResponse(
    @Schema(description = "Identificador único", example = "1") Long id,
    @Schema(description = "Identificador do cliente", example = "2") Long userId,
    @Schema(description = "Status atual do pedido") OrderStatus status,
    @Schema(description = "Valor total do pedido", example = "599.80") BigDecimal totalAmount,
    @Schema(description = "Itens do pedido") List<OrderItemResponse> items,
    @Schema(description = "Motivo da falha, presente apenas quando o status é FAILED")
        String failureReason,
    @Schema(description = "Data e hora de criação") LocalDateTime createdAt,
    @Schema(description = "Data e hora da última atualização") LocalDateTime updatedAt) {

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
