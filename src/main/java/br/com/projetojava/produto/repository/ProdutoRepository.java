package br.com.projetojava.produto.repository;

import br.com.projetojava.produto.domain.Produto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA para a entidade {@link Produto}.
 *
 * <p>Estende {@link JpaRepository} para herdar as operações CRUD padrão. Consultas customizadas são
 * declaradas aqui como métodos derivados ou {@code @Query}.
 */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

  /**
   * Retorna todos os produtos com o status de ativo informado.
   *
   * @param ativo {@code true} para produtos ativos, {@code false} para inativos
   * @return lista de produtos filtrada pelo status
   */
  List<Produto> findAllByAtivo(boolean ativo);
}
