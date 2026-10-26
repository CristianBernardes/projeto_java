package br.com.projetojava.product.service;

import br.com.projetojava.product.dto.CreateProductRequest;
import br.com.projetojava.product.dto.ProductResponse;
import br.com.projetojava.product.dto.UpdateProductRequest;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import java.util.List;

/**
 * Contract for the product domain use cases.
 *
 * <p>The controller and any other consumer depend on this interface, never on the concrete
 * implementation. This ensures the <em>Dependency Inversion Principle</em> (DIP) and allows
 * replacing or decorating the implementation without changing consumers.
 */
public interface ProductServicePort {

  /**
   * Lists all active products.
   *
   * @return list of response DTOs for active products
   */
  List<ProductResponse> listActive();

  /**
   * Finds a product by its identifier.
   *
   * @param id product identifier
   * @return response DTO of the found product
   * @throws ResourceNotFoundException if the product does not exist
   */
  ProductResponse findById(Long id);

  /**
   * Creates a new product from the provided data.
   *
   * @param request creation data
   * @return response DTO of the created product
   */
  ProductResponse create(CreateProductRequest request);

  /**
   * Updates name, description and price of an existing product.
   *
   * @param id product identifier
   * @param request new data
   * @return response DTO with updated data
   * @throws ResourceNotFoundException if the product does not exist
   */
  ProductResponse update(Long id, UpdateProductRequest request);

  /**
   * Deactivates a product, making it unavailable in the catalog.
   *
   * @param id product identifier
   * @throws ResourceNotFoundException if the product does not exist
   */
  void deactivate(Long id);
}
