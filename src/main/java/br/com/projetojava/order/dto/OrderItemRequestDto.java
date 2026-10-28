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
@Schema(description = "Um item de pedido em uma requisição de criação de pedido")
public record OrderItemRequestDto(
    @Schema(
            description = "Identificador do produto",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O id do produto é obrigatório.")
        Long productId,
    @Schema(
            description = "Quantidade solicitada",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
        Integer quantity) {}
