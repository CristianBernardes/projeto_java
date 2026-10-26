package br.com.projetojava.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Input DTO for partial product update.
 *
 * @param name new name (required, max 255 characters)
 * @param description new description (optional)
 * @param price new price (required, positive)
 */
@Schema(description = "Data for updating an existing product")
public record UpdateProductRequest(
    @Schema(
            description = "New product name",
            example = "Mechanical Keyboard Pro",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required.")
        @Size(max = 255, message = "Name must have at most 255 characters.")
        String name,
    @Schema(description = "New product description", example = "Cherry MX Blue switches, RGB")
        String description,
    @Schema(
            description = "New unit price",
            example = "349.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.01", message = "Price must be positive.")
        BigDecimal price) {}
