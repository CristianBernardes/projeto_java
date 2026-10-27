package br.com.projetojava.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Input DTO for a single line item requested in a new order.
 *
 * @param productId identifier of the product being purchased (required)
 * @param quantity quantity requested (required, at least 1)
 */
@Schema(description = "A single line item in an order request")
public record OrderItemRequestDto(
    @Schema(
            description = "Product identifier",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product id is required.")
        Long productId,
    @Schema(
            description = "Quantity requested",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity) {}
