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
@Schema(description = "Dados para criar um novo produto")
public record CreateProductRequest(
    @Schema(
            description = "Nome do produto",
            example = "Mechanical Keyboard",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String name,
    @Schema(description = "Descrição detalhada do produto", example = "Cherry MX Red switches, RGB")
        String description,
    @Schema(
            description = "Preço unitário",
            example = "299.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser positivo.")
        BigDecimal price,
    @Schema(
            description = "Quantidade inicial em estoque",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O estoque é obrigatório.")
        @Min(value = 0, message = "O estoque não pode ser negativo.")
        Integer stock) {}
