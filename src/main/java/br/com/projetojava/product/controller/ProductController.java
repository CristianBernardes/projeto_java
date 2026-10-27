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
@Tag(name = "Products", description = "Product catalog management")
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
      summary = "List active products",
      description = "Returns all products with active status.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List returned successfully",
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
      summary = "Find product by ID",
      description = "Returns a product by its unique identifier.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(
      @Parameter(description = "Product identifier", example = "1", required = true) @PathVariable
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
      summary = "Create product",
      description = "Creates a new product in the catalog.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            headers = @Header(name = "Location", description = "URL of the created product"),
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(
            responseCode = "422",
            description = "Invalid input data",
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
   * Updates name, description and price of an existing product.
   *
   * @param id product identifier
   * @param request validated new data
   * @return {@code 200 OK} with the updated product
   */
  @Operation(
      summary = "Update product",
      description = "Updates name, description and price of an existing product.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "422",
            description = "Invalid input data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @Parameter(description = "Product identifier", example = "1", required = true) @PathVariable
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
      summary = "Deactivate product",
      description =
          "Deactivates a product (soft delete). The product is not removed from the database, only"
              + " marked as inactive.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(responseCode = "204", description = "Product deactivated successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deactivate(
      @Parameter(description = "Product identifier", example = "1", required = true) @PathVariable
          Long id) {
    productService.deactivate(id);
    return ResponseEntity.noContent().build();
  }
}
