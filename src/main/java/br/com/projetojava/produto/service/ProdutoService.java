package br.com.projetojava.produto.service;

import br.com.projetojava.produto.domain.Produto;
import br.com.projetojava.produto.dto.AtualizarProdutoRequest;
import br.com.projetojava.produto.dto.CriarProdutoRequest;
import br.com.projetojava.produto.dto.ProdutoResponse;
import br.com.projetojava.produto.repository.ProdutoRepository;
import br.com.projetojava.shared.exception.RecursoNaoEncontradoException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação dos casos de uso do domínio de produtos.
 *
 * <p>Orquestra as operações entre o controller e o repositório, delegando as regras de negócio à
 * entidade {@link Produto}.
 *
 * <p>Implementa {@link ProdutoServicePort} para respeitar o <em>Dependency Inversion
 * Principle</em>: os consumidores dependem da interface, não desta classe concreta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService implements ProdutoServicePort {

  private final ProdutoRepository produtoRepository;

  /**
   * Lista todos os produtos ativos.
   *
   * @return lista de DTOs de resposta dos produtos ativos
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProdutoResponse> listarAtivos() {
    log.info("Listando produtos ativos.");
    return produtoRepository.findAllByAtivo(true).stream().map(ProdutoResponse::de).toList();
  }

  /**
   * Busca um produto pelo seu identificador.
   *
   * @param id identificador do produto
   * @return DTO de resposta do produto encontrado
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  @Override
  @Transactional(readOnly = true)
  public ProdutoResponse buscarPorId(Long id) {
    log.info("Buscando produto id={}.", id);
    return ProdutoResponse.de(buscarEntidade(id));
  }

  /**
   * Cria um novo produto a partir dos dados fornecidos.
   *
   * @param request dados de criação
   * @return DTO de resposta do produto criado
   */
  @Override
  @Transactional
  public ProdutoResponse criar(CriarProdutoRequest request) {
    log.info("Criando produto nome='{}'.", request.nome());
    Produto produto =
        Produto.criar(request.nome(), request.descricao(), request.preco(), request.estoque());
    return ProdutoResponse.de(produtoRepository.save(produto));
  }

  /**
   * Atualiza nome, descrição e preço de um produto existente.
   *
   * @param id identificador do produto
   * @param request novos dados
   * @return DTO de resposta com os dados atualizados
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  @Override
  @Transactional
  public ProdutoResponse atualizar(Long id, AtualizarProdutoRequest request) {
    log.info("Atualizando produto id={}.", id);
    Produto produto = buscarEntidade(id);
    produto.setNome(request.nome());
    produto.setDescricao(request.descricao());
    produto.atualizarPreco(request.preco());
    return ProdutoResponse.de(produtoRepository.save(produto));
  }

  /**
   * Desativa um produto, tornando-o indisponível no catálogo.
   *
   * @param id identificador do produto
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  @Override
  @Transactional
  public void desativar(Long id) {
    log.info("Desativando produto id={}.", id);
    Produto produto = buscarEntidade(id);
    produto.desativar();
    produtoRepository.save(produto);
  }

  // ── Helpers privados ────────────────────────────────────────────────────────

  private Produto buscarEntidade(Long id) {
    return produtoRepository
        .findById(id)
        .orElseThrow(
            () -> new RecursoNaoEncontradoException("Produto não encontrado. id=" + id));
  }
}
