package br.com.projetojava.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Input DTO for product creation.
 *
 * @param name product name (required, max 255 characters)
 * @param description optional description
 * @param price unit price (required, positive)
 * @param stock initial stock quantity (required, non-negative)
 */
@Schema(description = "Data for creating a new product")
public record CreateProductRequest(
    @Schema(
            description = "Product name",
            example = "Mechanical Keyboard",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required.")
        @Size(max = 255, message = "Name must have at most 255 characters.")
        String name,
    @Schema(description = "Detailed product description", example = "Cherry MX Red switches, RGB")
        String description,
    @Schema(
            description = "Unit price",
            example = "299.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.01", message = "Price must be positive.")
        BigDecimal price,
    @Schema(
            description = "Initial stock quantity",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Stock is required.")
        @Min(value = 0, message = "Stock cannot be negative.")
        Integer stock) {}
