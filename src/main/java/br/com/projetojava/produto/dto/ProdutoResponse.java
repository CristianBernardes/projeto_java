package br.com.projetojava.produto.dto;

import br.com.projetojava.produto.domain.Produto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de saída que representa um produto na resposta da API.
 *
 * @param id identificador único
 * @param nome nome do produto
 * @param descricao descrição
 * @param preco preço unitário
 * @param estoque quantidade em estoque
 * @param ativo indica se o produto está ativo
 * @param criadoEm data/hora de criação
 * @param atualizadoEm data/hora da última atualização
 */
@Schema(description = "Representação de um produto")
public record ProdutoResponse(
    @Schema(description = "Identificador único", example = "1") Long id,
    @Schema(description = "Nome do produto", example = "Teclado Mecânico") String nome,
    @Schema(description = "Descrição do produto", example = "Switch Cherry MX Red, RGB, ABNT2")
        String descricao,
    @Schema(description = "Preço unitário em reais", example = "299.90") BigDecimal preco,
    @Schema(description = "Quantidade em estoque", example = "10") int estoque,
    @Schema(description = "Indica se o produto está ativo", example = "true") boolean ativo,
    @Schema(description = "Data e hora de criação") LocalDateTime criadoEm,
    @Schema(description = "Data e hora da última atualização") LocalDateTime atualizadoEm) {

  /**
   * Converte uma entidade {@link Produto} para este DTO de resposta.
   *
   * @param produto entidade a converter
   * @return DTO preenchido com os dados da entidade
   */
  public static ProdutoResponse de(Produto produto) {
    return new ProdutoResponse(
        produto.getId(),
        produto.getNome(),
        produto.getDescricao(),
        produto.getPreco(),
        produto.getEstoque(),
        produto.isAtivo(),
        produto.getCriadoEm(),
        produto.getAtualizadoEm());
  }
}
