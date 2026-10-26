package br.com.projetojava.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada para atualização parcial de um produto.
 *
 * @param nome novo nome (obrigatório, máx. 255 caracteres)
 * @param descricao nova descrição (opcional)
 * @param preco novo preço (obrigatório, positivo)
 */
@Schema(description = "Dados para atualização de um produto existente")
public record AtualizarProdutoRequest(
    @Schema(
            description = "Novo nome do produto",
            example = "Teclado Mecânico Pro",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres.")
        String nome,
    @Schema(
            description = "Nova descrição do produto",
            example = "Switch Cherry MX Blue, RGB, ABNT2")
        String descricao,
    @Schema(
            description = "Novo preço unitário em reais",
            example = "349.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço deve ser positivo.")
        BigDecimal preco) {}
