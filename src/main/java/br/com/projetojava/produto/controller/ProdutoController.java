package br.com.projetojava.produto.controller;

import br.com.projetojava.produto.dto.AtualizarProdutoRequest;
import br.com.projetojava.produto.dto.CriarProdutoRequest;
import br.com.projetojava.produto.dto.ProdutoResponse;
import br.com.projetojava.produto.service.ProdutoServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller REST para o recurso {@code /api/v1/produtos}.
 *
 * <p>Expõe os endpoints de CRUD do catálogo de produtos, delegando toda a lógica ao {@link
 * ProdutoServicePort}.
 */
@Tag(name = "Produtos", description = "Gerenciamento do catálogo de produtos")
@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

  private final ProdutoServicePort produtoService;

  /**
   * Lista todos os produtos ativos.
   *
   * @return {@code 200 OK} com a lista de produtos
   */
  @Operation(
      summary = "Listar produtos ativos",
      description = "Retorna todos os produtos com status ativo.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista retornada com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ProdutoResponse.class))))
      })
  @GetMapping
  public ResponseEntity<List<ProdutoResponse>> listar() {
    return ResponseEntity.ok(produtoService.listarAtivos());
  }

  /**
   * Busca um produto pelo seu identificador.
   *
   * @param id identificador do produto
   * @return {@code 200 OK} com o produto encontrado, ou {@code 404} se não existir
   */
  @Operation(
      summary = "Buscar produto por ID",
      description = "Retorna um produto pelo seu identificador único.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProdutoResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProdutoResponse> buscarPorId(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id) {
    return ResponseEntity.ok(produtoService.buscarPorId(id));
  }

  /**
   * Cria um novo produto.
   *
   * @param request dados de criação validados
   * @return {@code 201 Created} com o produto criado e o header {@code Location}
   */
  @Operation(
      summary = "Criar produto",
      description = "Cria um novo produto no catálogo.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            headers = @Header(name = "Location", description = "URL do produto criado"),
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProdutoResponse.class))),
        @ApiResponse(
            responseCode = "422",
            description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping
  public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request) {
    ProdutoResponse criado = produtoService.criar(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criado.id())
            .toUri();
    return ResponseEntity.created(location).body(criado);
  }

  /**
   * Atualiza nome, descrição e preço de um produto existente.
   *
   * @param id identificador do produto
   * @param request novos dados validados
   * @return {@code 200 OK} com o produto atualizado
   */
  @Operation(
      summary = "Atualizar produto",
      description = "Atualiza nome, descrição e preço de um produto existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProdutoResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "422",
            description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProdutoResponse> atualizar(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id,
      @Valid @RequestBody AtualizarProdutoRequest request) {
    return ResponseEntity.ok(produtoService.atualizar(id, request));
  }

  /**
   * Desativa um produto (soft delete).
   *
   * @param id identificador do produto
   * @return {@code 204 No Content}
   */
  @Operation(
      summary = "Desativar produto",
      description =
          "Desativa um produto (soft delete). O produto não é removido do banco, apenas marcado como inativo.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> desativar(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id) {
    produtoService.desativar(id);
    return ResponseEntity.noContent().build();
  }
}
