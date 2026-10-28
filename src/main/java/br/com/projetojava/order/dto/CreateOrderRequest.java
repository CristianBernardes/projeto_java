package br.com.projetojava.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Input DTO for creating a new order.
 *
 * <p>The order is accepted and persisted synchronously in {@link
 * br.com.projetojava.order.domain.OrderStatus#PENDING}; stock reservation happens asynchronously
 * afterward (see the {@code order.messaging} package).
 *
 * @param deliveryAddress where the order should be delivered (required)
 * @param items line items to purchase (required, at least one)
 */
@Schema(description = "Dados para criar um novo pedido")
public record CreateOrderRequest(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        DeliveryAddressRequest deliveryAddress,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "O pedido deve conter ao menos um item.")
        @Valid
        List<OrderItemRequestDto> items) {}
