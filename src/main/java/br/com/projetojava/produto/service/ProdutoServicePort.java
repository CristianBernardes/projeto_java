package br.com.projetojava.produto.service;

import br.com.projetojava.produto.dto.AtualizarProdutoRequest;
import br.com.projetojava.produto.dto.CriarProdutoRequest;
import br.com.projetojava.produto.dto.ProdutoResponse;
import br.com.projetojava.shared.exception.RecursoNaoEncontradoException;
import java.util.List;

/**
 * Contrato dos casos de uso do domínio de produtos.
 *
 * <p>O controller e qualquer outro consumidor dependem desta interface, nunca da implementação
 * concreta. Isso garante o <em>Dependency Inversion Principle</em> (DIP) e permite substituir ou
 * decorar a implementação sem alterar os consumidores.
 */
public interface ProdutoServicePort {

  /**
   * Lista todos os produtos ativos.
   *
   * @return lista de DTOs de resposta dos produtos ativos
   */
  List<ProdutoResponse> listarAtivos();

  /**
   * Busca um produto pelo seu identificador.
   *
   * @param id identificador do produto
   * @return DTO de resposta do produto encontrado
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  ProdutoResponse buscarPorId(Long id);

  /**
   * Cria um novo produto a partir dos dados fornecidos.
   *
   * @param request dados de criação
   * @return DTO de resposta do produto criado
   */
  ProdutoResponse criar(CriarProdutoRequest request);

  /**
   * Atualiza nome, descrição e preço de um produto existente.
   *
   * @param id identificador do produto
   * @param request novos dados
   * @return DTO de resposta com os dados atualizados
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  ProdutoResponse atualizar(Long id, AtualizarProdutoRequest request);

  /**
   * Desativa um produto, tornando-o indisponível no catálogo.
   *
   * @param id identificador do produto
   * @throws RecursoNaoEncontradoException se o produto não existir
   */
  void desativar(Long id);
}
