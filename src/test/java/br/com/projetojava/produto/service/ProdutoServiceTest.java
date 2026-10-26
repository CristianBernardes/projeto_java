package br.com.projetojava.produto.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.projetojava.produto.domain.Produto;
import br.com.projetojava.produto.dto.CriarProdutoRequest;
import br.com.projetojava.produto.dto.ProdutoResponse;
import br.com.projetojava.produto.repository.ProdutoRepository;
import br.com.projetojava.shared.exception.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceTest {

  @Mock private ProdutoRepository produtoRepository;

  @InjectMocks private ProdutoService produtoService;

  @Test
  @DisplayName("criar — deve persistir e retornar o produto criado")
  void criar_devePersistirERetornarProduto() {
    CriarProdutoRequest request =
        new CriarProdutoRequest("Teclado", "Mecânico", new BigDecimal("299.90"), 10);

    Produto produto = Produto.criar("Teclado", "Mecânico", new BigDecimal("299.90"), 10);
    when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

    ProdutoResponse response = produtoService.criar(request);

    assertThat(response.nome()).isEqualTo("Teclado");
    assertThat(response.preco()).isEqualByComparingTo("299.90");
    assertThat(response.estoque()).isEqualTo(10);
    verify(produtoRepository).save(any(Produto.class));
  }

  @Test
  @DisplayName("buscarPorId — deve lançar RecursoNaoEncontradoException quando não existir")
  void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
    when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> produtoService.buscarPorId(99L))
        .isInstanceOf(RecursoNaoEncontradoException.class)
        .hasMessageContaining("99");
  }

  @Test
  @DisplayName("desativar — deve chamar desativar na entidade e salvar")
  void desativar_deveChamarDesativarESalvar() {
    Produto produto = Produto.criar("Mouse", null, new BigDecimal("89.90"), 5);
    when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
    when(produtoRepository.save(produto)).thenReturn(produto);

    produtoService.desativar(1L);

    assertThat(produto.isAtivo()).isFalse();
    verify(produtoRepository).save(produto);
  }
}
