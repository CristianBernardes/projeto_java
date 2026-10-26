package br.com.projetojava.produto.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de domínio que representa um produto no catálogo.
 *
 * <p>Segue o padrão de entidade rica: as regras de negócio que dizem respeito ao estado do produto
 * ficam aqui, não nos serviços.
 */
@Entity
@Table(name = "produtos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Produto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Setter
  private String nome;

  @Column(columnDefinition = "TEXT")
  @Setter
  private String descricao;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal preco;

  @Column(nullable = false)
  private int estoque;

  @Column(nullable = false)
  private boolean ativo = true;

  @Column(name = "criado_em", nullable = false, updatable = false)
  private LocalDateTime criadoEm;

  @Column(name = "atualizado_em", nullable = false)
  private LocalDateTime atualizadoEm;

  /**
   * Cria um novo produto com os dados fornecidos.
   *
   * @param nome nome do produto
   * @param descricao descrição opcional
   * @param preco preço unitário (deve ser positivo)
   * @param estoque quantidade inicial em estoque (deve ser não-negativa)
   * @throws IllegalArgumentException se preço ou estoque forem inválidos
   */
  public static Produto criar(String nome, String descricao, BigDecimal preco, int estoque) {
    validarPreco(preco);
    validarEstoque(estoque);

    Produto produto = new Produto();
    produto.nome = nome;
    produto.descricao = descricao;
    produto.preco = preco;
    produto.estoque = estoque;
    produto.ativo = true;
    produto.criadoEm = LocalDateTime.now();
    produto.atualizadoEm = LocalDateTime.now();
    return produto;
  }

  /**
   * Atualiza o preço do produto.
   *
   * @param novoPreco novo preço (deve ser positivo)
   * @throws IllegalArgumentException se o preço for inválido
   */
  public void atualizarPreco(BigDecimal novoPreco) {
    validarPreco(novoPreco);
    this.preco = novoPreco;
    this.atualizadoEm = LocalDateTime.now();
  }

  /**
   * Adiciona unidades ao estoque.
   *
   * @param quantidade quantidade a adicionar (deve ser positiva)
   * @throws IllegalArgumentException se a quantidade for inválida
   */
  public void adicionarEstoque(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException("Quantidade a adicionar deve ser positiva.");
    }
    this.estoque += quantidade;
    this.atualizadoEm = LocalDateTime.now();
  }

  /**
   * Remove unidades do estoque.
   *
   * @param quantidade quantidade a remover (deve ser positiva e não exceder o estoque atual)
   * @throws IllegalArgumentException se a quantidade for inválida ou insuficiente
   */
  public void removerEstoque(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException("Quantidade a remover deve ser positiva.");
    }
    if (quantidade > this.estoque) {
      throw new IllegalArgumentException(
          "Estoque insuficiente. Disponível: %d, solicitado: %d."
              .formatted(this.estoque, quantidade));
    }
    this.estoque -= quantidade;
    this.atualizadoEm = LocalDateTime.now();
  }

  /** Desativa o produto, tornando-o indisponível no catálogo. */
  public void desativar() {
    this.ativo = false;
    this.atualizadoEm = LocalDateTime.now();
  }

  /** Reativa o produto. */
  public void ativar() {
    this.ativo = true;
    this.atualizadoEm = LocalDateTime.now();
  }

  // ── Validações privadas ──────────────────────────────────────────────────────

  private static void validarPreco(BigDecimal preco) {
    if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Preço deve ser positivo.");
    }
  }

  private static void validarEstoque(int estoque) {
    if (estoque < 0) {
      throw new IllegalArgumentException("Estoque não pode ser negativo.");
    }
  }

  // ── equals / hashCode baseados em chave de negócio ──────────────────────────

  /**
   * Dois produtos são iguais se possuem o mesmo {@code id} persistido.
   *
   * <p>Produtos transientes (id {@code null}) nunca são iguais entre si, evitando problemas em
   * coleções antes do {@code persist}.
   *
   * @param o objeto a comparar
   * @return {@code true} se ambos têm o mesmo id não-nulo
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Produto other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Hash constante baseado no tipo, garantindo estabilidade em {@code Set} e {@code Map} mesmo
   * antes do {@code persist} (quando o id ainda é {@code null}).
   *
   * @return hash constante para todas as instâncias de {@link Produto}
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
