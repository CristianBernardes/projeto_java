package br.com.projetojava.product.dto;

import br.com.projetojava.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output DTO representing a product in the API response.
 *
 * @param id unique identifier
 * @param name product name
 * @param description description
 * @param price unit price
 * @param stock stock quantity
 * @param active indicates whether the product is active
 * @param createdAt creation date/time
 * @param updatedAt last update date/time
 */
@Schema(description = "Product representation")
public record ProductResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Product name", example = "Mechanical Keyboard") String name,
    @Schema(description = "Product description", example = "Cherry MX Red switches, RGB")
        String description,
    @Schema(description = "Unit price", example = "299.90") BigDecimal price,
    @Schema(description = "Stock quantity", example = "10") int stock,
    @Schema(description = "Indicates whether the product is active", example = "true")
        boolean active,
    @Schema(description = "Creation date and time") LocalDateTime createdAt,
    @Schema(description = "Last update date and time") LocalDateTime updatedAt) {

  /**
   * Converts a {@link Product} entity to this response DTO.
   *
   * @param product entity to convert
   * @return DTO populated with entity data
   */
  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.isActive(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
