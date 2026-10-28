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
 * @param stock new stock quantity (required, non-negative)
 */
@Schema(description = "Dados para atualizar um produto existente")
public record UpdateProductRequest(
    @Schema(
            description = "Novo nome do produto",
            example = "Mechanical Keyboard Pro",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String name,
    @Schema(description = "Nova descrição do produto", example = "Cherry MX Blue switches, RGB")
        String description,
    @Schema(
            description = "Novo preço unitário",
            example = "349.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser positivo.")
        BigDecimal price,
    @Schema(
            description = "Nova quantidade em estoque",
            example = "15",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O estoque é obrigatório.")
        @Min(value = 0, message = "O estoque não pode ser negativo.")
        Integer stock) {}
