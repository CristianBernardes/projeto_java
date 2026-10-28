package br.com.projetojava.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for the delivery address of an order.
 *
 * @param street street name and number (required)
 * @param city delivery city (required)
 * @param state two-letter state code (required, e.g., "SP")
 * @param zip postal code (required)
 */
@Schema(description = "Endereço de entrega do pedido")
public record DeliveryAddressRequest(
    @Schema(
            description = "Nome da rua e número",
            example = "Rua das Flores, 123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A rua é obrigatória.")
        String street,
    @Schema(
            description = "Cidade",
            example = "São Paulo",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A cidade é obrigatória.")
        String city,
    @Schema(
            description = "Código do estado",
            example = "SP",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O estado é obrigatório.")
        @Size(min = 2, max = 2, message = "O estado deve ter um código de 2 letras.")
        String state,
    @Schema(description = "CEP", example = "01310-100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O CEP é obrigatório.")
        String zip) {}
