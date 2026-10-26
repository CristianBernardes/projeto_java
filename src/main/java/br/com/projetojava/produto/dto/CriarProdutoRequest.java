package br.com.projetojava.produto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada para criação de um produto.
 *
 * @param nome nome do produto (obrigatório, máx. 255 caracteres)
 * @param descricao descrição opcional
 * @param preco preço unitário (obrigatório, positivo)
 * @param estoque quantidade inicial em estoque (obrigatório, não-negativa)
 */
@Schema(description = "Dados para criação de um novo produto")
public record CriarProdutoRequest(
    @Schema(
            description = "Nome do produto",
            example = "Teclado Mecânico",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres.")
        String nome,
    @Schema(
            description = "Descrição detalhada do produto",
            example = "Switch Cherry MX Red, RGB, ABNT2")
        String descricao,
    @Schema(
            description = "Preço unitário em reais",
            example = "299.90",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço deve ser positivo.")
        BigDecimal preco,
    @Schema(
            description = "Quantidade inicial em estoque",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Estoque é obrigatório.")
        @Min(value = 0, message = "Estoque não pode ser negativo.")
        Integer estoque) {}
