package br.com.projetojava.product.controller;

import br.com.projetojava.product.dto.CreateProductRequest;
import br.com.projetojava.product.dto.ProductResponse;
import br.com.projetojava.product.dto.UpdateProductRequest;
import br.com.projetojava.product.service.ProductServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * REST controller for the {@code /api/v1/products} resource.
 *
 * <p>Exposes CRUD endpoints for the product catalog, delegating all logic to {@link
 * ProductServicePort}.
 */
@Tag(name = "Produtos", description = "Gerenciamento do catálogo de produtos")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductServicePort productService;

  /**
   * Lists all active products.
   *
   * @return {@code 200 OK} with the list of products
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
                    array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
      })
  @GetMapping
  public ResponseEntity<List<ProductResponse>> list() {
    return ResponseEntity.ok(productService.listActive());
  }

  /**
   * Finds a product by its identifier.
   *
   * @param id product identifier
   * @return {@code 200 OK} with the found product, or {@code 404} if not found
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
                    schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id) {
    return ResponseEntity.ok(productService.findById(id));
  }

  /**
   * Creates a new product.
   *
   * @param request validated creation data
   * @return {@code 201 Created} with the created product and the {@code Location} header
   */
  @Operation(
      summary = "Criar produto",
      description = "Cria um novo produto no catálogo.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            headers = @Header(name = "Location", description = "URL do produto criado"),
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(
            responseCode = "422",
            description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
    ProductResponse created = productService.create(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  /**
   * Updates name, description, price and stock of an existing product.
   *
   * @param id product identifier
   * @param request validated new data
   * @return {@code 200 OK} with the updated product
   */
  @Operation(
      summary = "Atualizar produto",
      description = "Atualiza nome, descrição, preço e estoque de um produto existente.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class))),
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
  public ResponseEntity<ProductResponse> update(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id,
      @Valid @RequestBody UpdateProductRequest request) {
    return ResponseEntity.ok(productService.update(id, request));
  }

  /**
   * Deactivates a product (soft delete).
   *
   * @param id product identifier
   * @return {@code 204 No Content}
   */
  @Operation(
      summary = "Desativar produto",
      description =
          "Desativa um produto (exclusão lógica). O produto não é removido do banco de dados,"
              + " apenas marcado como inativo.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deactivate(
      @Parameter(description = "Identificador do produto", example = "1", required = true)
          @PathVariable
          Long id) {
    productService.deactivate(id);
    return ResponseEntity.noContent().build();
  }
}
