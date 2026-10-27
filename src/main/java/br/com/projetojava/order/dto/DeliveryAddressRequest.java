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
@Schema(description = "Delivery address for the order")
public record DeliveryAddressRequest(
    @Schema(
            description = "Street name and number",
            example = "Rua das Flores, 123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Street is required.")
        String street,
    @Schema(
            description = "City",
            example = "São Paulo",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "City is required.")
        String city,
    @Schema(description = "State code", example = "SP", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "State is required.")
        @Size(min = 2, max = 2, message = "State must be a 2-letter code.")
        String state,
    @Schema(
            description = "Postal code",
            example = "01310-100",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Zip code is required.")
        String zip) {}
